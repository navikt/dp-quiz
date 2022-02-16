package no.nav.dagpenger.quiz.mediator.db

import kotliquery.Parameter
import kotliquery.action.UpdateQueryAction
import kotliquery.queryOf
import no.nav.dagpenger.model.factory.BaseFaktumFactory
import no.nav.dagpenger.model.faktum.Dokument
import no.nav.dagpenger.model.faktum.Inntekt
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.faktum.Tekst
import org.intellij.lang.annotations.Language
import java.time.LocalDate
import java.time.LocalDateTime

internal class FaktumUpdateBuilder(søknad: Søknad, indeks: Int, rootId: Int) {
    //language=PostgreSQL
    private val whereClause = """
               WHERE id = (SELECT faktum_verdi.id FROM faktum_verdi, soknad, faktum
                WHERE soknad.id = faktum_verdi.soknad_id AND faktum.id = faktum_verdi.faktum_id
                AND soknad.uuid = :uuid AND faktum_verdi.indeks = :indeks AND faktum.root_id = :rootId)
            """
    private val whereClauseParameters = mapOf(
        "uuid" to søknad.uuid,
        "indeks" to indeks,
        "rootId" to rootId
    )

    fun build(svar: Any?, besvartAv: Int?): UpdateQueryAction {
        return when (svar) {
            null -> TODO("unn")
            is Boolean -> build(BooleanBuilder(svar, besvartAv))
            is LocalDate -> build(LocalDateBuilder(svar, besvartAv))
            is Inntekt -> build(InntektBuilder(svar, besvartAv))
            is Tekst -> build(TekstBuilder(svar, besvartAv))
            is Dokument -> build(DokumentBuilder(svar, besvartAv))
            else -> TODO("uhu")
        }
    }

    private fun build(updateClauseBuilder: UpdateClauseBuilder): UpdateQueryAction {
        return queryOf(
            statement = updateClauseBuilder.updateClause() + " " + whereClause,
            paramMap = whereClauseParameters + updateClauseBuilder.paramMap()
        ).asUpdate
    }

    private sealed interface UpdateClauseBuilder {
        fun updateClause(): String
        fun paramMap(): Map<String, Any?> = emptyMap()
    }

    private class DokumentBuilder(private val svar: Dokument, private val besvartAv: Int?) : UpdateClauseBuilder {
        private lateinit var opplastet: LocalDateTime
        private lateinit var url: String

        init {
            svar.reflection { l, u ->
                opplastet = l
                url = u
            }
        }

        @Language("PostgreSQL")
        override fun updateClause() =
            """
        WITH inserted_id AS (INSERT INTO dokument (opplastet, url) VALUES (:opplastet, :url) returning id)
        UPDATE faktum_verdi SET dokument_id = (SELECT id FROM inserted_id) , besvart_av = $besvartAv , opprettet=NOW() AT TIME ZONE 'utc' 
           """

        override fun paramMap(): Map<String, Any?> {
            return mapOf(
                "opplastet" to Parameter(opplastet, LocalDateTime::class.java),
                "url" to Parameter(url, String::class.java),
            )
        }
    }

    private class TekstBuilder(private val svar: Tekst, private val besvartAv: Int?) : UpdateClauseBuilder {

        override fun updateClause() =
            """UPDATE faktum_verdi  SET tekst = :tekst , besvart_av = $besvartAv , opprettet=NOW() AT TIME ZONE 'utc'"""

        override fun paramMap(): Map<String, Any?> {
            return mapOf(
                "tekst" to svar.verdi,
            )
        }
    }

    private class InntektBuilder(private val svar: Inntekt, private val besvartAv: Int?) : UpdateClauseBuilder {
        private val aarligInntekt = svar.reflection { a, _, _, _ -> a }

        override fun updateClause() =
            """UPDATE faktum_verdi  SET aarlig_inntek = $aarligInntekt , besvart_av = $besvartAv , opprettet=NOW() AT TIME ZONE 'utc' """
    }

    private class LocalDateBuilder(private val svar: LocalDate, private val besvartAv: Int?) : UpdateClauseBuilder {

        override fun updateClause() =
            """UPDATE faktum_verdi  SET dato = :dato , besvart_av = $besvartAv , opprettet=NOW() AT TIME ZONE 'utc' """

        override fun paramMap() = mapOf(
            "dato" to Parameter(svar, LocalDate::class.java)
        )
    }

    private class BooleanBuilder(private val svar: Boolean, private val besvartAv: Int?) : UpdateClauseBuilder {

        override fun updateClause() =
            """UPDATE faktum_verdi  SET boolsk = $svar , besvart_av = $besvartAv , opprettet=NOW() AT TIME ZONE 'utc' """
    }

    private class NullFaktumBuilder : UpdateClauseBuilder {
        override fun updateClause() =
            """UPDATE faktum_verdi  SET boolsk = NULL , aarlig_inntekt = NULL, dokument_id = NULL, dato = NULL, heltall = NULL, opprettet=NOW() AT TIME ZONE 'utc' """
    }
}
