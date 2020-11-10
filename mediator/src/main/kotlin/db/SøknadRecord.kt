package db

import DataSourceBuilder.dataSource
import kotliquery.action.ExecuteQueryAction
import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import no.nav.dagpenger.model.factory.FaktaRegel
import no.nav.dagpenger.model.faktagrupper.Faktagrupper
import no.nav.dagpenger.model.faktagrupper.Versjon
import no.nav.dagpenger.model.faktum.Dokument
import no.nav.dagpenger.model.faktum.Faktum
import no.nav.dagpenger.model.faktum.FaktumId
import no.nav.dagpenger.model.faktum.GeneratorFaktum
import no.nav.dagpenger.model.faktum.GrunnleggendeFaktum
import no.nav.dagpenger.model.faktum.Inntekt
import no.nav.dagpenger.model.faktum.Inntekt.Companion.årlig
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.faktum.TemplateFaktum
import no.nav.dagpenger.model.faktum.UtledetFaktum
import no.nav.dagpenger.model.faktum.ValgFaktum
import no.nav.dagpenger.model.visitor.SøknadVisitor
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

// Understands a relational representation of a Søknad
class SøknadRecord : SøknadPersistence {
    private lateinit var originalSvar: Map<String, Any?>

    override fun ny(fnr: String, type: Versjon.FaktagrupperType): Faktagrupper {
        return Versjon.siste.faktagrupper(fnr, type).also { faktagrupper ->
            NySøknad(faktagrupper.søknad, type)
            originalSvar = svarMap(faktagrupper.søknad)
        }
    }

    private fun svarMap(søknad: Søknad): Map<String, Any?> = søknad.map { faktum ->
        faktum.id to (if (faktum.erBesvart()) faktum.svar() else null)
    }.toMap()

    override fun hent(uuid: UUID, type: Versjon.FaktagrupperType?): Faktagrupper {
        data class SoknadRad(val fnr: String, val versjonId: Int, var typeId: Int)

        val rad = using(sessionOf(dataSource)) { session ->
            if (type != null) {
                session.run( //language=PostgreSQL
                    queryOf("UPDATE soknad SET sesjon_type_id = ? WHERE uuid = ?", type.id, uuid).asUpdate
                )
            }

            session.run(
                queryOf( //language=PostgreSQL
                    "SELECT fnr, versjon_id, sesjon_type_id FROM soknad WHERE uuid = ?",
                    uuid
                ).map { row ->
                    SoknadRad(row.string(1), row.int(2), row.int(3))
                }.asSingle
            )
        } ?: throw IllegalArgumentException("Ugyldig uuid: $uuid")

        return Versjon.id(rad.versjonId)
            .faktagrupper(rad.fnr, Versjon.FaktagrupperType.fromId(rad.typeId), uuid)
            .also { faktagrupper ->
                svarList(uuid).forEach { row ->
                    faktagrupper.søknad.idOrNull(row.root_id indeks row.indeks)?.also { faktum ->
                        if (row.heltall != null) (faktum as Faktum<Int>).besvar(row.heltall)
                        if (row.janei != null) (faktum as Faktum<Boolean>).besvar(row.janei)
                        if (row.dato != null) (faktum as Faktum<LocalDate>).besvar(row.dato)
                        if (row.inntekt != null) (faktum as Faktum<Inntekt>).besvar(row.inntekt)
                        if (row.opplastet != null && row.url != null) (faktum as Faktum<Dokument>).besvar(
                            Dokument(
                                row.opplastet,
                                row.url
                            )
                        )
                    }
                }
            }.also { faktagrupper ->
                originalSvar = svarMap(faktagrupper.søknad)
            }
    }

    private infix fun Int.indeks(indeks: Int) = if (indeks == 0) this.toString() else "$this.$indeks"

    private fun svarList(uuid: UUID): List<FaktumVerdiRow> {
        return using(sessionOf(dataSource)) { session ->
            session.run(
                queryOf( //language=PostgreSQL
                    """
                        WITH soknad_faktum AS (SELECT faktum.id as faktum_id, faktum.root_id AS root_id, soknad.id AS soknad_id FROM soknad, faktum
                                WHERE faktum.versjon_id = soknad.versjon_id AND faktum.regel IS NULL AND soknad.uuid = ?)
                            SELECT 
                                soknad_faktum.root_id as root_id,
                                faktum_verdi.indeks as indeks,
                                faktum_verdi.heltall AS heltall, 
                                faktum_verdi.ja_nei AS ja_nei, 
                                faktum_verdi.dato AS dato, 
                                faktum_verdi.aarlig_inntekt AS aarlig_inntekt, 
                                dokument.url AS url, 
                                dokument.opplastet AS opplastet 
                            FROM faktum_verdi
                            JOIN soknad_faktum ON faktum_verdi.soknad_id = soknad_faktum.soknad_id 
                                AND faktum_verdi.faktum_id = soknad_faktum.faktum_id
                            LEFT JOIN dokument ON faktum_verdi.dokument_id = dokument.id
                            ORDER BY indeks""",
                    uuid
                ).map {
                    FaktumVerdiRow(
                        it.int("root_id"),
                        it.int("indeks"),
                        it.intOrNull("heltall"),
                        it.anyOrNull("ja_nei") as Boolean?,
                        it.localDateOrNull("dato"),
                        it.doubleOrNull("aarlig_inntekt")?.årlig,
                        it.stringOrNull("url"),
                        it.localDateTimeOrNull("opplastet")
                    )
                }.asList
            )
        }!!
    }

    private class FaktumVerdiRow(
        val root_id: Int,
        val indeks: Int,
        val heltall: Int?,
        val janei: Boolean?,
        val dato: LocalDate?,
        val inntekt: Inntekt?,
        val url: String?,
        val opplastet: LocalDateTime?
    )

    private fun sqlToInsert(svar: Any?): String {
        //language=PostgreSQL
        return when (svar) {
            null -> """UPDATE faktum_verdi  SET ja_nei = NULL , aarlig_inntekt = NULL, dokument_id = NULL, dato = NULL, heltall = NULL, opprettet=NOW() AT TIME ZONE 'utc' """
            is Boolean -> """UPDATE faktum_verdi  SET ja_nei = $svar , opprettet=NOW() AT TIME ZONE 'utc' """
            is Inntekt -> """UPDATE faktum_verdi  SET aarlig_inntekt = ${svar.reflection { aarlig, _, _, _ -> aarlig }} , opprettet=NOW() AT TIME ZONE 'utc' """
            is LocalDate -> """UPDATE faktum_verdi  SET dato = '$svar',  opprettet=NOW() AT TIME ZONE 'utc' """
            is Int -> """UPDATE faktum_verdi  SET heltall = $svar,  opprettet=NOW() AT TIME ZONE 'utc' """
            is Dokument -> """WITH inserted_id AS (INSERT INTO dokument (url, opplastet) VALUES (${svar.reflection { opplastet, url -> "'$url', '$opplastet'" }}) returning id) 
|                               UPDATE faktum_verdi SET dokument_id = (SELECT id FROM inserted_id), opprettet=NOW() AT TIME ZONE 'utc' """.trimMargin()
            else -> throw IllegalArgumentException("Ugyldig type: ${svar.javaClass}")
        } + """WHERE id = (SELECT faktum_verdi.id FROM faktum_verdi, soknad, faktum
            WHERE soknad.id = faktum_verdi.soknad_id AND faktum.id = faktum_verdi.faktum_id AND soknad.uuid = ? AND faktum_verdi.indeks = ? AND faktum.root_id = ?  )"""
    }

    override fun lagre(søknad: Søknad): Boolean {
        val nyeSvar = svarMap(søknad)

        originalSvar.filterNot { (id, svar) -> nyeSvar[id] == svar }.forEach { (id, svar) ->
            val (rootId, indeks) = søknad.id(id).reflection { rootId, indeks -> rootId to indeks }

            using(sessionOf(dataSource)) { session ->
                session.run(arkiverFaktum(søknad, rootId, indeks))
                session.run(oppdaterFaktum(nyeSvar[id], søknad, indeks, rootId))
            }
        }

        nyeSvar.filterNot { (id, _) -> originalSvar.containsKey(id) }.forEach { (id, svar) ->
            val (rootId, indeks) = søknad.id(id).reflection { rootId, indeks -> rootId to indeks }
            using(sessionOf(dataSource)) { session ->
                session.run(opprettTemplateFaktum(indeks, søknad, rootId))
                if (svar != null) session.run(oppdaterFaktum(svar, søknad, indeks, rootId))
            }
        }

        return true
    }

    private fun oppdaterFaktum(svar: Any?, søknad: Søknad, indeks: Int, rootId: Int): ExecuteQueryAction =
        queryOf(sqlToInsert(svar), søknad.uuid, indeks, rootId).asExecute

    private fun arkiverFaktum(søknad: Søknad, rootId: Int, indeks: Int): ExecuteQueryAction =
        queryOf( //language=PostgreSQL
            """INSERT INTO gammel_faktum_verdi (soknad_id, faktum_id, indeks, ja_nei, aarlig_inntekt, dokument_id, dato, heltall, opprettet)
            SELECT soknad_id,
                   faktum_verdi.faktum_id,
                   faktum_verdi.indeks,
                   faktum_verdi.ja_nei,
                   faktum_verdi.aarlig_inntekt,
                   faktum_verdi.dokument_id,
                   faktum_verdi.dato,
                   faktum_verdi.heltall,
                   faktum_verdi.opprettet
            FROM faktum_verdi,
                 faktum,
                 soknad
            WHERE faktum_verdi.faktum_id = faktum.id
              AND faktum_verdi.soknad_id = soknad.id
              AND soknad.uuid = ?
              AND faktum.root_id = ?
              AND faktum_verdi.indeks = ?
                """.trimMargin(),
            søknad.uuid,
            rootId,
            indeks
        ).asExecute

    private fun opprettTemplateFaktum(indeks: Int, søknad: Søknad, rootId: Int): ExecuteQueryAction =
        queryOf( //language=PostgreSQL
            """INSERT INTO faktum_verdi (indeks, soknad_id, faktum_id)
            SELECT ?, soknad.id, faktum.id
            FROM soknad,
                 faktum
            WHERE soknad.uuid = ?
              AND faktum.versjon_id = soknad.versjon_id
              AND faktum.root_id = ?
            """.trimMargin(),
            indeks,
            søknad.uuid,
            rootId
        ).asExecute

    override fun opprettede(fnr: String): Map<LocalDateTime, UUID> {
        return using(sessionOf(dataSource)) { session ->
            session.run(
                queryOf( //language=PostgreSQL
                    "SELECT opprettet, uuid FROM soknad WHERE fnr = ?",
                    fnr
                ).map { it.localDateTime(1) to UUID.fromString(it.string(2)) }.asList
            )
        }.toMap()
    }

    private class NySøknad(søknad: Søknad, private val type: Versjon.FaktagrupperType) : SøknadVisitor {
        private var faktaId = 0
        private var versjonId = 0
        private var rootId = 0
        private var indeks = 0
        private val faktumList = mutableListOf<Faktum<*>>()

        init {
            søknad.accept(this)
        }

        override fun preVisit(søknad: Søknad, fnr: String, versjonId: Int, uuid: UUID) {
            this.versjonId = versjonId
            faktaId = using(sessionOf(dataSource)) { session ->
                session.run(
                    queryOf(
                        "INSERT INTO soknad(uuid, versjon_id, fnr, sesjon_type_id) VALUES (?, ?, ?, ?) returning id",
                        uuid,
                        versjonId,
                        fnr,
                        type.id
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

        override fun preVisit(
            faktum: ValgFaktum,
            id: String,
            avhengigeFakta: Set<Faktum<*>>,
            avhengerAvFakta: Set<Faktum<*>>,
            underordnedeJa: Set<Faktum<Boolean>>,
            underordnedeNei: Set<Faktum<Boolean>>,
            clazz: Class<Boolean>
        ) {
            skrivFaktumVerdi(faktum)
        }

        private fun skrivFaktumVerdi(faktum: Faktum<*>) {
            if (faktum in faktumList) return else faktumList.add(faktum)
            using(sessionOf(dataSource)) { session ->
                session.run(
                    queryOf( //language=PostgreSQL
                        """INSERT INTO faktum_verdi
                            (soknad_id, indeks, faktum_id)
                        VALUES (?, ?,
                                (SELECT id FROM faktum WHERE versjon_id = ? AND root_id = ?))""".trimMargin(),
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
