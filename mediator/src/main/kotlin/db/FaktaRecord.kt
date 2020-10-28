package db

import DataSourceBuilder.dataSource
import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import no.nav.dagpenger.model.factory.FaktaRegel
import no.nav.dagpenger.model.fakta.Dokument
import no.nav.dagpenger.model.fakta.Fakta
import no.nav.dagpenger.model.fakta.Faktum
import no.nav.dagpenger.model.fakta.FaktumId
import no.nav.dagpenger.model.fakta.GeneratorFaktum
import no.nav.dagpenger.model.fakta.GrunnleggendeFaktum
import no.nav.dagpenger.model.fakta.Inntekt
import no.nav.dagpenger.model.fakta.Inntekt.Companion.årlig
import no.nav.dagpenger.model.fakta.Rolle
import no.nav.dagpenger.model.fakta.TemplateFaktum
import no.nav.dagpenger.model.fakta.UtledetFaktum
import no.nav.dagpenger.model.søknad.Søknad
import no.nav.dagpenger.model.søknad.Versjon
import no.nav.dagpenger.model.visitor.FaktaVisitor
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

// Understands a relational representation of a Fakta
class FaktaRecord : FaktaPersistance {
    private lateinit var originalSvar: Map<String, Any?>

    override fun ny(fnr: String, søknadType: Versjon.Type): Søknad {
        return Versjon.siste.søknad(fnr, søknadType).also { søknad ->
            NyFakta(søknad.fakta)
            originalSvar = svarMap(søknad.fakta)
        }
    }

    private fun svarMap(fakta: Fakta) = fakta.map { faktum ->
        faktum.id to (if (faktum.erBesvart()) faktum.svar() else null)
    }.toMap()

    override fun hent(uuid: UUID, søknadType: Versjon.Type): Søknad {
        val (fnr, versjonId) = using(sessionOf(dataSource)) { session ->
            session.run(
                queryOf(
                    """SELECT fnr, versjon_id FROM fakta WHERE uuid = ? """,
                    uuid
                ).map { row ->
                    row.string(1) to row.int(2)
                }.asSingle
            )
        } ?: throw IllegalArgumentException("Ugyldig uuid: $uuid")

        return Versjon.id(versjonId).søknad(fnr, søknadType).also { søknad ->
            søknad.fakta.forEach { faktum ->
                val (rootId, indeks) = faktum.reflection { rootId, indeks -> rootId to indeks }
                val svar = svar(uuid = uuid, rootId = rootId, indeks = indeks)
                when (faktum.clazz()) {
                    Int::class.java -> { if (svar.heltall != null) (faktum as Faktum<Int>).besvar(svar.heltall) }
                    Boolean::class.java -> { if (svar.janei != null) (faktum as Faktum<Boolean>).besvar(svar.janei) }
                    LocalDate::class.java -> { if (svar.dato != null) (faktum as Faktum<LocalDate>).besvar(svar.dato) }
                    Dokument::class.java -> {}
                    Inntekt::class.java -> { if (svar.inntekt != null) (faktum as Faktum<Inntekt>).besvar(svar.inntekt) }
                    else -> throw java.lang.IllegalArgumentException("Ukjent faktumklasse ${faktum.clazz()}")
                }
            }
        }
    }

    private fun svar(uuid: UUID, rootId: Int, indeks: Int): FaktumVerdiRow {
        return using(sessionOf(dataSource)) { session ->
            session.run(
                queryOf(
                    """SELECT 
                                faktum_verdi.heltall AS heltall, 
                                faktum_verdi.ja_nei AS ja_nei, 
                                faktum_verdi.dato AS dato, 
                                faktum_verdi.dokument_id AS dokument_id, 
                                faktum_verdi.aarlig_inntekt AS aarlig_inntekt 
                            FROM faktum_verdi
                            WHERE faktum_verdi.id = (SELECT faktum_verdi.id FROM faktum_verdi, fakta, faktum
                                WHERE fakta.id = faktum_verdi.fakta_id AND faktum.id = faktum_verdi.faktum_id AND fakta.uuid = ? AND faktum_verdi.indeks = ? AND faktum.root_id = ?  )""",
                    uuid,
                    indeks,
                    rootId
                ).map {
                    FaktumVerdiRow(
                        it.intOrNull("heltall"),
                        it.anyOrNull("ja_nei") as Boolean?,
                        it.localDateOrNull("dato"),
                        it.intOrNull("dokument_id"),
                        it.doubleOrNull("aarlig_inntekt")?.årlig
                    )
                }.asSingle
            )
        }!!
    }

    private class FaktumVerdiRow(
        val heltall: Int?,
        val janei: Boolean?,
        val dato: LocalDate?,
        val dokumentId: Int?,
        val inntekt: Inntekt?
    )

    private fun insertSQL(svar: Any?): String {
        return when (svar) {
            null -> """UPDATE faktum_verdi  SET ja_nei = NULL , aarlig_inntekt = NULL, dokument_id = NULL, dato = NULL, heltall = NULL, opprettet=NOW() AT TIME ZONE 'utc' """
            is Boolean -> """UPDATE faktum_verdi  SET ja_nei = $svar , opprettet=NOW() AT TIME ZONE 'utc' """
            is Inntekt -> """UPDATE faktum_verdi  SET aarlig_inntekt = ${svar.reflection { aarlig, _, _, _ -> aarlig }} , opprettet=NOW() AT TIME ZONE 'utc' """
            is LocalDate -> """UPDATE faktum_verdi  SET dato = '$svar',  opprettet=NOW() AT TIME ZONE 'utc' """
            is Int -> """UPDATE faktum_verdi  SET heltall = $svar,  opprettet=NOW() AT TIME ZONE 'utc' """
            else -> throw IllegalArgumentException("Ugyldig type: ${svar.javaClass}")
        } + """WHERE id = (SELECT faktum_verdi.id FROM faktum_verdi, fakta, faktum
            WHERE fakta.id = faktum_verdi.fakta_id AND faktum.id = faktum_verdi.faktum_id AND fakta.uuid = ? AND faktum_verdi.indeks = ? AND faktum.root_id = ?  )"""
    }

    override fun lagre(fakta: Fakta): Boolean {
        val nySvar = svarMap(fakta)
        originalSvar.forEach { id, svar ->
            if (nySvar[id] == svar) return@forEach
            val (rootId, indeks) = fakta.id(id).reflection { rootId, indeks -> rootId to indeks }

            using(sessionOf(dataSource)) { session ->
                session.run(
                    queryOf(
                        insertSQL(nySvar[id]),
                        fakta.uuid,
                        indeks,
                        rootId
                    ).asExecute
                )
            }
        }
        return true
    }

    override fun opprettede(fnr: String): Map<LocalDateTime, UUID> {
        return using(sessionOf(dataSource)) { session ->
            session.run(
                queryOf(
                    "SELECT opprettet, uuid FROM fakta WHERE fnr = ?",
                    fnr
                ).map { it.localDateTime(1) to UUID.fromString(it.string(2)) }.asList
            )
        }.toMap()
    }

    private class NyFakta(fakta: Fakta) : FaktaVisitor {
        private var faktaId = 0
        private var versjonId = 0
        private var rootId = 0
        private var indeks = 0

        private val faktumList = mutableListOf<Faktum<*>>()

        init {
            fakta.accept(this)
        }

        override fun preVisit(fakta: Fakta, fnr: String, versjonId: Int, uuid: UUID) {
            this.versjonId = versjonId
            faktaId = using(sessionOf(dataSource)) { session ->
                session.run(
                    queryOf(
                        "INSERT INTO fakta(uuid, versjon_id, fnr) VALUES (?, ?, ?) returning id",
                        uuid,
                        versjonId,
                        fnr
                    ).map { it.int(1) }.asSingle
                )!!
            }
        }

        override fun visit(faktumId: FaktumId, rootId: Int, indeks: Int) {
            this.rootId = rootId
            this.indeks = indeks
        }

        override fun <R : Comparable<R>> visit(
            faktum: GrunnleggendeFaktum<R>,
            tilstand: Faktum.FaktumTilstand,
            id: String,
            avhengigeFakta: Set<Faktum<*>>,
            avhengerAvFaktum: Set<Faktum<*>>,
            roller: Set<Rolle>,
            clazz: Class<R>
        ) {
            skrivFaktumVerdi(faktum)
        }

        override fun <R : Comparable<R>> visit(
            faktum: GeneratorFaktum,
            id: String,
            avhengigeFakta: Set<Faktum<*>>,
            avhengerAvFaktum: Set<Faktum<*>>,
            templates: List<Faktum<*>>,
            roller: Set<Rolle>,
            clazz: Class<R>
        ) {
            skrivFaktumVerdi(faktum)
        }

        override fun <R : Comparable<R>> visit(
            faktum: TemplateFaktum<R>,
            id: String,
            avhengigeFakta: Set<Faktum<*>>,
            avhengerAvFaktum: Set<Faktum<*>>,
            roller: Set<Rolle>,
            clazz: Class<R>
        ) {
            skrivFaktumVerdi(faktum)
        }

        override fun <R : Comparable<R>> preVisit(
            faktum: UtledetFaktum<R>,
            id: String,
            avhengigeFakta: Set<Faktum<*>>,
            avhengerAvFaktum: Set<Faktum<*>>,
            children: Set<Faktum<*>>,
            clazz: Class<R>,
            regel: FaktaRegel<R>
        ) {
            skrivFaktumVerdi(faktum)
        }

        private fun skrivFaktumVerdi(faktum: Faktum<*>) {
            if (faktum in faktumList) return else faktumList.add(faktum)
            using(sessionOf(dataSource)) { session ->
                session.run(
                    queryOf(
                        """INSERT INTO faktum_verdi
                            (fakta_id, indeks, faktum_id) 
                            VALUES (?, ?, 
                                (SELECT id FROM faktum WHERE versjon_id = ? AND root_id = ?)
                            )""".trimMargin(),
                        faktaId,
                        indeks,
                        versjonId,
                        rootId
                    ).asExecute
                )
            }
        }
    }
}
