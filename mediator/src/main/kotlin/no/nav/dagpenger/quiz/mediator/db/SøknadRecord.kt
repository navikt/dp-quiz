package no.nav.dagpenger.quiz.mediator.db

import kotliquery.action.ExecuteQueryAction
import kotliquery.action.UpdateQueryAction
import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import no.nav.dagpenger.model.faktum.Dokument
import no.nav.dagpenger.model.faktum.Envalg
import no.nav.dagpenger.model.faktum.Faktum
import no.nav.dagpenger.model.faktum.FaktumId
import no.nav.dagpenger.model.faktum.Flervalg
import no.nav.dagpenger.model.faktum.Identer
import no.nav.dagpenger.model.faktum.Identer.Ident
import no.nav.dagpenger.model.faktum.Inntekt
import no.nav.dagpenger.model.faktum.Inntekt.Companion.årlig
import no.nav.dagpenger.model.faktum.Land
import no.nav.dagpenger.model.faktum.Periode
import no.nav.dagpenger.model.faktum.Prosessnavn
import no.nav.dagpenger.model.faktum.Prosessversjon
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.faktum.Tekst
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.model.seksjon.Versjon
import no.nav.dagpenger.quiz.mediator.db.PostgresDataSourceBuilder.dataSource
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

// Understands a relational representation of a Søknad
class SøknadRecord : SøknadPersistence {
    private val personRecord = PersonRecord()

    override fun ny(
        identer: Identer,
        type: Versjon.UserInterfaceType,
        prosessVersjon: Prosessversjon,
        uuid: UUID
    ): Søknadprosess {
        val person = personRecord.hentEllerOpprettPerson(identer)
        return Versjon.id(prosessVersjon).søknadprosess(person, type, uuid).also { søknadprosess ->
            NySøknad(søknadprosess.søknad, type)
        }
    }

    override fun hent(uuid: UUID, type: Versjon.UserInterfaceType?): Søknadprosess {
        data class SoknadRad(val personId: UUID, val navn: String, val versjonId: Int, var typeId: Int)
        data class Prosess(override val id: String) : Prosessnavn

        val rad = using(sessionOf(dataSource)) { session ->
            if (type != null) {
                session.run( //language=PostgreSQL
                    queryOf("UPDATE soknad SET sesjon_type_id = ? WHERE uuid = ?", type.id, uuid).asUpdate
                )
            }

            session.run(
                queryOf( //language=PostgreSQL
                    "SELECT soknad.person_id, versjon.navn , versjon.versjon_id, soknad.sesjon_type_id FROM soknad JOIN V1_PROSESSVERSJON as versjon ON (versjon.id = soknad.versjon_id) WHERE uuid = ?",
                    uuid
                ).map { row ->
                    SoknadRad(UUID.fromString(row.string(1)), row.string(2), row.int(3), row.int(4))
                }.asSingle
            )
        } ?: throw IllegalArgumentException("Søknad finnes ikke, uuid: $uuid")

        return Versjon.id(Prosessversjon(Prosess(rad.navn), rad.versjonId))
            .søknadprosess(
                person = personRecord.hentPerson(rad.personId),
                type = Versjon.UserInterfaceType.fromId(rad.typeId),
                uuid = uuid
            )
            .also { søknadprosess ->
                svarList(uuid).onEach { row ->
                    søknadprosess.søknad.idOrNull(row.id)?.also { rehydrerFaktum(row, it) }
                }
            }
    }

    @Suppress("UNCHECKED_CAST")
    private fun rehydrerFaktum(row: FaktumVerdiRow, faktum: Faktum<*>) {
        if (row.heltall != null) {
            (faktum as Faktum<Int>).rehydrer(row.heltall, row.besvartAv)
        }
        if (row.boolsk != null) {
            (faktum as Faktum<Boolean>).rehydrer(row.boolsk, row.besvartAv)
        }
        if (row.dato != null) {
            (faktum as Faktum<LocalDate>).rehydrer(row.dato, row.besvartAv)
        }
        if (row.inntekt != null) {
            (faktum as Faktum<Inntekt>).rehydrer(row.inntekt, row.besvartAv)
        }
        if (row.desimaltall != null) {
            (faktum as Faktum<Double>).rehydrer(row.desimaltall, row.besvartAv)
        }
        if (row.tekst != null) {
            (faktum as Faktum<Tekst>).rehydrer(row.tekst, row.besvartAv)
        }

        if (row.land != null) {
            (faktum as Faktum<Land>).rehydrer(row.land, row.besvartAv)
        }

        if (row.opplastet != null && row.urn != null) {
            (faktum as Faktum<Dokument>).rehydrer(
                Dokument(
                    row.opplastet,
                    row.urn
                ),
                row.besvartAv
            )
        }
        if (row.envalg != null) {
            (faktum as Faktum<Envalg>).rehydrer(row.envalg, row.besvartAv)
        }
        if (row.flervalg != null) {
            (faktum as Faktum<Flervalg>).rehydrer(row.flervalg, row.besvartAv)
        }
        if (row.fom != null) {
            (faktum as Faktum<Periode>).rehydrer(
                Periode(
                    row.fom,
                    row.tom
                ),
                row.besvartAv
            )
        }
    }

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
                                faktum_verdi.desimaltall AS desimaltall, 
                                faktum_verdi.boolsk AS boolsk, 
                                faktum_verdi.dato AS dato, 
                                faktum_verdi.aarlig_inntekt AS aarlig_inntekt, 
                                besvarer.identifikator AS besvartAv,
                                dokument.urn AS urn, 
                                dokument.opplastet AS opplastet,
                                envalg.verdier AS envalgVerdier,
                                flervalg.verdier AS flervalgVerdier,
                                faktum_verdi.tekst AS tekst,
                                faktum_verdi.land as land,
                                periode.fom AS fom,
                                periode.tom AS tom
                            FROM faktum_verdi
                            JOIN soknad_faktum ON faktum_verdi.soknad_id = soknad_faktum.soknad_id 
                                AND faktum_verdi.faktum_id = soknad_faktum.faktum_id
                            LEFT JOIN dokument ON faktum_verdi.dokument_id = dokument.id
                            LEFT JOIN periode ON faktum_verdi.periode_id = periode.id
                            LEFT JOIN valgte_verdier envalg ON faktum_verdi.envalg_id = envalg.id
                            LEFT JOIN valgte_verdier flervalg ON faktum_verdi.flervalg_id = flervalg.id
                            LEFT JOIN besvarer ON faktum_verdi.besvart_av = besvarer.id
                            ORDER BY indeks""",
                    uuid
                ).map {
                    FaktumVerdiRow(
                        it.int("root_id"),
                        it.int("indeks"),
                        it.intOrNull("heltall"),
                        it.anyOrNull("boolsk") as Boolean?,
                        it.underlying.getObject("dato", LocalDate::class.java),
                        it.doubleOrNull("aarlig_inntekt")?.årlig,
                        it.stringOrNull("urn"),
                        it.localDateTimeOrNull("opplastet"),
                        it.stringOrNull("besvartAv"),
                        it.doubleOrNull("desimaltall"),
                        it.arrayOrNull<String>("envalgVerdier")?.let { verdier -> Envalg(*verdier) },
                        it.arrayOrNull<String>("flervalgVerdier")?.let { verdier -> Flervalg(*verdier) },
                        it.stringOrNull("tekst")?.let { verdi -> Tekst(verdi) },
                        it.stringOrNull("land")?.let { verdi -> Land(verdi) },
                        it.localDateOrNull("fom"),
                        it.localDateOrNull("tom")
                    )
                }.asList
            )
        }
    }

    private class FaktumVerdiRow(
        val root_id: Int,
        val indeks: Int,
        val heltall: Int?,
        val boolsk: Boolean?,
        val dato: LocalDate?,
        val inntekt: Inntekt?,
        val urn: String?,
        val opplastet: LocalDateTime?,
        val besvartAv: String?,
        val desimaltall: Double?,
        val envalg: Envalg?,
        val flervalg: Flervalg?,
        val tekst: Tekst?,
        val land: Land?,
        val fom: LocalDate?,
        val tom: LocalDate?,
        val id: FaktumId = if (indeks == 0) FaktumId(root_id) else FaktumId(root_id).medIndeks(indeks)
    )

    override fun lagre(søknad: Søknad): Boolean {
        val nyeSvar: MutableMap<String, Faktum<*>?> = svarMap(søknad)
        val originalSvar: MutableMap<String, Faktum<*>?> = svarMap(hent(søknad.uuid).søknad)

        slettDødeTemplatefakta(søknad, nyeSvar, originalSvar)

        originalSvar
            .filterNot { (id, faktum) -> nyeSvar[id]?.svar() == faktum?.svar() }.forEach { (id, originaltFaktum) ->
                val (rootId, indeks) = søknad.id(id).reflection { rootId, indeks -> rootId to indeks }

                using(sessionOf(dataSource)) { session ->
                    if (originaltFaktum?.svar() != null) {
                        session.run(arkiverFaktum(søknad, rootId, indeks))
                    }
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
        // TODO: burde ikke alltid returnere true?? hva hvis noe går galt som ikke kræsjer? hvorfor boolean?
        return true
    }

    private fun svarMap(søknad: Søknad): MutableMap<String, Faktum<*>?> = søknad.associate { faktum ->
        faktum.id to (if (faktum.erBesvart()) faktum else null)
    }.toMutableMap()

    private fun slettDødeTemplatefakta(
        søknad: Søknad,
        nyeSvar: Map<String, Faktum<*>?>,
        originalSvar: MutableMap<String, Faktum<*>?>
    ) {
        originalSvar.keys.toSet()
            .subtract(nyeSvar.keys.toSet())
            .map { FaktumId(it).reflection { rootId, indeks -> Triple(rootId, indeks, it) } }
            .forEach { (rootId, indeks, id) ->
                using(sessionOf(dataSource)) { session ->
                    val originaltFaktum = originalSvar[id]?.svar()
                    if (originaltFaktum != null) {
                        session.run(arkiverFaktum(søknad = søknad, rootId = rootId, indeks = indeks))
                    }
                    session.run(slettDødeFaktum(søknad = søknad, rootId, indeks))
                    originalSvar.remove(id)
                }
            }
    }

    private fun slettDødeFaktum(søknad: Søknad, rootId: Int, indeks: Int) = queryOf(
        //language=PostgreSQL
        """
              DELETE FROM faktum_verdi
              WHERE id IN 
                (SELECT faktum_verdi.id as faktum_id FROM soknad, faktum_verdi, faktum
                  WHERE faktum_verdi.soknad_id = soknad.id 
                        AND faktum_verdi.faktum_id = faktum.id 
                        AND soknad.uuid = ? 
                        AND faktum.root_id = ? 
                        AND faktum_verdi.indeks = ?
                )
        """.trimIndent(),
        søknad.uuid,
        rootId,
        indeks
    ).asExecute

    private fun oppdaterFaktum(faktum: Faktum<*>?, søknad: Søknad, indeks: Int, rootId: Int): UpdateQueryAction =
        FaktumUpdateBuilder(søknad, indeks, rootId).build(faktum?.svar(), besvart(faktum?.besvartAv()))

    private fun besvart(besvartAv: String?): Int? {
        val besvarer = besvartAv ?: return null
        return using(sessionOf(dataSource)) { session ->
            session.run(
                queryOf( //language=PostgreSQL
                    """
                         SELECT id FROM besvarer WHERE identifikator = :besvarer""",
                    mapOf("besvarer" to besvarer)
                ).map { it.int(1) }.asSingle
            ) ?: session.run(
                queryOf( //language=PostgreSQL
                    """
                    INSERT INTO besvarer (identifikator) VALUES (:besvarer) RETURNING id
                    """.trimIndent(),
                    mapOf("besvarer" to besvarer)
                ).map { it.int(1) }.asSingle
            )
        }
    }

    private fun arkiverFaktum(søknad: Søknad, rootId: Int, indeks: Int): ExecuteQueryAction =
        queryOf( //language=PostgreSQL
            """INSERT INTO gammel_faktum_verdi (soknad_id, faktum_id, indeks, boolsk, aarlig_inntekt, dokument_id, dato, heltall, envalg_id, flervalg_id, tekst,land, periode_id, opprettet, besvart_av, desimaltall)
            SELECT soknad_id,
                   faktum_verdi.faktum_id,
                   faktum_verdi.indeks,
                   faktum_verdi.boolsk,
                   faktum_verdi.aarlig_inntekt,
                   faktum_verdi.dokument_id,
                   faktum_verdi.dato,
                   faktum_verdi.heltall,
                   faktum_verdi.envalg_id,
                   faktum_verdi.flervalg_id,
                   faktum_verdi.tekst,
                   faktum_verdi.land,
                   faktum_verdi.periode_id,
                   faktum_verdi.opprettet,
                   faktum_verdi.besvart_av,
                   faktum_verdi.desimaltall
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
            SELECT :indeks, soknad.id, faktum.id
            FROM soknad,
                 faktum
            WHERE soknad.uuid = :soknadUuid
              AND faktum.versjon_id = soknad.versjon_id
              AND faktum.root_id = :rootId
            """.trimMargin(),
            mapOf(
                "indeks" to indeks,
                "soknadUuid" to søknad.uuid,
                "rootId" to rootId
            )
        ).asExecute

    override fun opprettede(identer: Identer): Map<LocalDateTime, UUID> {
        return using(sessionOf(dataSource)) { session ->
            session.run(
                queryOf( //language=PostgreSQL
                    "SELECT opprettet, uuid FROM soknad WHERE person_id = (SELECT person_id FROM folkeregisterident WHERE verdi = ?)",
                    identer.first { it.type == Ident.Type.FOLKEREGISTERIDENT && !it.historisk }.id
                ).map { it.localDateTime(1) to UUID.fromString(it.string(2)) }.asList
            )
        }.toMap()
    }

    override fun slett(uuid: UUID): Boolean {
        using(sessionOf(dataSource)) { session ->
            session.run( //language=PostgreSQL
                queryOf(
                    "CALL slett_soknad(:uuid)",
                    mapOf("uuid" to uuid)
                ).asUpdate
            )
        }
        return true
    }
}
