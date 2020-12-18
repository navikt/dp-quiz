package no.nav.dagpenger.quiz.mediator.db

import PostgresDataSourceBuilder.dataSource
import kotliquery.action.ExecuteQueryAction
import kotliquery.action.UpdateQueryAction
import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import no.nav.dagpenger.model.faktum.Dokument
import no.nav.dagpenger.model.faktum.Faktum
import no.nav.dagpenger.model.faktum.FaktumId
import no.nav.dagpenger.model.faktum.Identer
import no.nav.dagpenger.model.faktum.Identer.Ident
import no.nav.dagpenger.model.faktum.Inntekt
import no.nav.dagpenger.model.faktum.Inntekt.Companion.årlig
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.model.seksjon.Versjon
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

// Understands a relational representation of a Søknad
class SøknadRecord : SøknadPersistence {
    private val personRecord = PersonRecord()

    override fun ny(identer: Identer, type: Versjon.UserInterfaceType, versjonId: Int): Søknadprosess {
        val person = personRecord.hentEllerOpprettPerson(identer)
        return Versjon.id(versjonId).søknadprosess(person, type).also { søknadprosess ->
            NySøknad(søknadprosess.søknad, type)
        }
    }

    override fun hent(uuid: UUID, type: Versjon.UserInterfaceType?): Søknadprosess {
        data class SoknadRad(val personId: UUID, val versjonId: Int, var typeId: Int)

        val rad = using(sessionOf(dataSource)) { session ->
            if (type != null) {
                session.run( //language=PostgreSQL
                    queryOf("UPDATE soknad SET sesjon_type_id = ? WHERE uuid = ?", type.id, uuid).asUpdate
                )
            }

            session.run(
                queryOf( //language=PostgreSQL
                    "SELECT person_id, versjon_id, sesjon_type_id FROM soknad WHERE uuid = ?",
                    uuid
                ).map { row ->
                    SoknadRad(UUID.fromString(row.string(1)), row.int(2), row.int(3))
                }.asSingle
            )
        } ?: throw IllegalArgumentException("Søknad finnes ikke, uuid: $uuid")

        return Versjon.id(rad.versjonId)
            .søknadprosess(
                personRecord.hentPerson(rad.personId),
                Versjon.UserInterfaceType.fromId(rad.typeId),
                uuid
            )
            .also { søknadprosess ->
                svarList(uuid).onEach { row ->
                    søknadprosess.søknad.idOrNull(row.id)?.also { rehydrerFaktum(row, it) }
                }
            }
    }

    private fun rehydrerFaktum(row: FaktumVerdiRow, faktum: Faktum<*>) {
        if (row.heltall != null) (faktum as Faktum<Int>).rehydrer(row.heltall)
        if (row.janei != null) (faktum as Faktum<Boolean>).rehydrer(row.janei)
        if (row.dato != null) (faktum as Faktum<LocalDate>).rehydrer(row.dato)
        if (row.inntekt != null) (faktum as Faktum<Inntekt>).rehydrer(row.inntekt)
        if (row.opplastet != null && row.url != null) (faktum as Faktum<Dokument>).rehydrer(
            Dokument(
                row.opplastet,
                row.url
            )
        )
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
                        it.underlying.getObject("dato", LocalDate::class.java),
                        it.doubleOrNull("aarlig_inntekt")?.årlig,
                        it.stringOrNull("url"),
                        it.localDateTimeOrNull("opplastet")
                    )
                }.asList
            )
        }
    }

    private class FaktumVerdiRow(
        val root_id: Int,
        val indeks: Int,
        val heltall: Int?,
        val janei: Boolean?,
        val dato: LocalDate?,
        val inntekt: Inntekt?,
        val url: String?,
        val opplastet: LocalDateTime?,
        val id: FaktumId = if (indeks == 0) FaktumId(root_id) else FaktumId(root_id).medIndeks(indeks)
    )

    override fun lagre(søknad: Søknad): Boolean {
        val nyeSvar = svarMap(søknad)
        val originalSvar = svarMap(hent(søknad.uuid).søknad)

        slettDødeFakta(søknad, nyeSvar, originalSvar)
        originalSvar.filterNot { (id, svar) -> nyeSvar[id] == svar }.forEach { (id, _) ->
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

    private fun svarMap(søknad: Søknad): MutableMap<String, Any?> = søknad.map { faktum ->
        faktum.id to (if (faktum.erBesvart()) faktum.svar() else null)
    }.toMap().toMutableMap()

    private fun slettDødeFakta(søknad: Søknad, nyeSvar: Map<String, Any?>, originalSvar: MutableMap<String, Any?>) {
        originalSvar.keys.toSet()
            .subtract(nyeSvar.keys.toSet())
            .map { FaktumId(it).reflection { rootId, indeks -> Triple(rootId, indeks, it) } }
            .forEach { (rootId, indeks, id) ->
                using(sessionOf(dataSource)) { session ->
                    session.run(arkiverFaktum(søknad = søknad, rootId = rootId, indeks = indeks))
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

    private fun oppdaterFaktum(svar: Any?, søknad: Søknad, indeks: Int, rootId: Int): UpdateQueryAction =
        queryOf(sqlToInsert(svar), søknad.uuid, indeks, rootId).asUpdate

    private fun sqlToInsert(svar: Any?): String {
        return when (svar) {
            null -> """UPDATE faktum_verdi  SET ja_nei = NULL , aarlig_inntekt = NULL, dokument_id = NULL, dato = NULL, heltall = NULL, opprettet=NOW() AT TIME ZONE 'utc' """
            is Boolean -> """UPDATE faktum_verdi  SET ja_nei = $svar , opprettet=NOW() AT TIME ZONE 'utc' """
            is Inntekt -> """UPDATE faktum_verdi  SET aarlig_inntekt = ${svar.reflection { aarlig, _, _, _ -> aarlig }} , opprettet=NOW() AT TIME ZONE 'utc' """
            is LocalDate -> """UPDATE faktum_verdi  SET dato = '${tilPostgresDato(svar)}',  opprettet=NOW() AT TIME ZONE 'utc' """
            is Int -> """UPDATE faktum_verdi  SET heltall = $svar,  opprettet=NOW() AT TIME ZONE 'utc' """
            is Dokument -> """WITH inserted_id AS (INSERT INTO dokument (url, opplastet) VALUES (${svar.reflection { opplastet, url -> "'$url', '$opplastet'" }}) returning id) 
|                               UPDATE faktum_verdi SET dokument_id = (SELECT id FROM inserted_id), opprettet=NOW() AT TIME ZONE 'utc' """.trimMargin()
            else -> throw IllegalArgumentException("Ugyldig type: ${svar.javaClass}")
        } + """WHERE id = (SELECT faktum_verdi.id FROM faktum_verdi, soknad, faktum
            WHERE soknad.id = faktum_verdi.soknad_id AND faktum.id = faktum_verdi.faktum_id AND soknad.uuid = ? AND faktum_verdi.indeks = ? AND faktum.root_id = ?  )"""
    }

    private fun tilPostgresDato(localDate: LocalDate) =
        if (localDate == LocalDate.MAX) "infinity" else localDate.toString()

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
}
