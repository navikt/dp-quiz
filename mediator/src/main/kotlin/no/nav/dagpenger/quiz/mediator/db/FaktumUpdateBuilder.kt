package no.nav.dagpenger.quiz.mediator.db

import kotliquery.Parameter
import kotliquery.action.UpdateQueryAction
import kotliquery.queryOf
import no.nav.dagpenger.model.faktum.Dokument
import no.nav.dagpenger.model.faktum.Envalg
import no.nav.dagpenger.model.faktum.Flervalg
import no.nav.dagpenger.model.faktum.Inntekt
import no.nav.dagpenger.model.faktum.Land
import no.nav.dagpenger.model.faktum.Periode
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
            null -> build(NullFaktumBuilder)
            is Boolean -> build(BooleanBuilder(svar, besvartAv))
            is LocalDate -> build(LocalDateBuilder(svar, besvartAv))
            is Inntekt -> build(InntektBuilder(svar, besvartAv))
            is Int -> build(IntBuilder(svar, besvartAv))
            is Double -> build(DoubleBuilder(svar, besvartAv))
            is Envalg -> build(EnvalgBuilder(svar, besvartAv))
            is Flervalg -> build(FlervalgBuilder(svar, besvartAv))
            is Periode -> build(PeriodeBuilder(svar, besvartAv))
            is Land -> build(LandBuilder(svar, besvartAv))
            is Tekst -> build(TekstBuilder(svar, besvartAv))
            is Dokument -> build(DokumentBuilder(svar, besvartAv))
            else -> throw IllegalArgumentException("Ugyldig type: ${svar.javaClass}")
        }
    }

    private fun build(updateClauseBuilder: UpdateClauseBuilder): UpdateQueryAction {
        val statement =
            if (updateClauseBuilder.insertStmt() != null) {
                "WITH oppdatert_faktum_verdi_id AS (" + updateClauseBuilder.updateClause() + " " + whereClause + " RETURNING id) " + updateClauseBuilder.insertStmt()
            } else {
                updateClauseBuilder.updateClause() + " " + whereClause
            }

        return queryOf(
            statement = statement,
            paramMap = whereClauseParameters + updateClauseBuilder.paramMap()
        ).asUpdate
    }

    private interface UpdateClauseBuilder {
        fun updateClause(): String
        fun insertStmt(): String? = null
        fun paramMap(): Map<String, Any?> = emptyMap()
    }

    private class BooleanBuilder(private val svar: Boolean, private val besvartAv: Int?) : UpdateClauseBuilder {
        override fun updateClause() =
            """UPDATE faktum_verdi SET boolsk = $svar, besvart_av = $besvartAv, opprettet=NOW() AT TIME ZONE 'utc'"""
    }

    private class LocalDateBuilder(private val svar: LocalDate, private val besvartAv: Int?) : UpdateClauseBuilder {
        override fun updateClause() =
            """UPDATE faktum_verdi  SET dato = '${tilPostgresDato(svar)}' , besvart_av = $besvartAv , opprettet=NOW() AT TIME ZONE 'utc' """

        private fun tilPostgresDato(localDate: LocalDate) =
            if (localDate == LocalDate.MAX) "infinity" else localDate.toString()
    }

    private class InntektBuilder(svar: Inntekt, private val besvartAv: Int?) : UpdateClauseBuilder {
        private val aarligInntekt = svar.reflection { a, _, _, _ -> a }

        override fun updateClause() =
            """UPDATE faktum_verdi  SET aarlig_inntekt = $aarligInntekt , besvart_av = $besvartAv , opprettet=NOW() AT TIME ZONE 'utc' """
    }

    private class IntBuilder(private val svar: Int, private val besvartAv: Int?) : UpdateClauseBuilder {
        override fun updateClause() =
            """UPDATE faktum_verdi  SET heltall = $svar , besvart_av = $besvartAv , opprettet=NOW() AT TIME ZONE 'utc' """
    }

    private class DoubleBuilder(private val svar: Double, private val besvartAv: Int?) : UpdateClauseBuilder {
        override fun updateClause() =
            """UPDATE faktum_verdi  SET desimaltall = $svar , besvart_av = $besvartAv , opprettet=NOW() AT TIME ZONE 'utc' """
    }

    private class EnvalgBuilder(svar: Envalg, private val besvartAv: Int?) : UpdateClauseBuilder {
        private val arrayString = svar.joinToString { """"$it"""" }
        override fun updateClause() = // language=PostgreSQL
            """UPDATE faktum_verdi SET  besvart_av = $besvartAv, opprettet=NOW() AT TIME ZONE 'utc'"""

        override fun insertStmt() = // language=PostgreSQL
            """INSERT INTO valgte_verdier (faktum_verdi_id, verdier) VALUES ((SELECT id FROM oppdatert_faktum_verdi_id), '{$arrayString}')
               ON CONFLICT (faktum_verdi_id) DO UPDATE SET verdier = '{$arrayString}'
            """.trimMargin()
    }

    private class FlervalgBuilder(svar: Flervalg, private val besvartAv: Int?) : UpdateClauseBuilder {
        private val arrayString = svar.joinToString { """"$it"""" }

        @Language("PostgreSQL")
        override fun updateClause() = // language=PostgreSQL
            """UPDATE faktum_verdi SET  besvart_av = $besvartAv, opprettet=NOW() AT TIME ZONE 'utc'"""

        override fun insertStmt() = // language=PostgreSQL
            """INSERT INTO valgte_verdier (faktum_verdi_id, verdier) VALUES ((SELECT id FROM oppdatert_faktum_verdi_id), '{$arrayString}')
              ON CONFLICT (faktum_verdi_id) DO UPDATE SET verdier = '{$arrayString}'
            """.trimMargin()
    }

    private class PeriodeBuilder(svar: Periode, private val besvartAv: Int?) : UpdateClauseBuilder {
        private lateinit var fom: LocalDate
        private var tom: LocalDate? = null

        init {
            svar.reflection { f, t ->
                fom = f
                tom = t
            }
        }

        override fun updateClause() = // language=PostgreSQL
            """UPDATE faktum_verdi SET besvart_av = $besvartAv , opprettet=NOW() AT TIME ZONE 'utc'"""

        override fun insertStmt() = // language=PostgreSQL
            """INSERT INTO periode (faktum_verdi_id, fom, tom) VALUES ((SELECT id FROM oppdatert_faktum_verdi_id), :fom, :tom)
               ON CONFLICT (faktum_verdi_id) DO UPDATE SET fom = :fom, tom = :tom
            """.trimMargin()

        override fun paramMap(): Map<String, Any?> {
            return mapOf(
                "fom" to Parameter(fom, LocalDate::class.java),
                "tom" to Parameter(tom, LocalDate::class.java),
            )
        }
    }

    private class LandBuilder(private val svar: Land, private val besvartAv: Int?) : UpdateClauseBuilder {
        override fun updateClause() =
            """UPDATE faktum_verdi  SET land = :land , besvart_av = $besvartAv , opprettet=NOW() AT TIME ZONE 'utc'"""

        override fun paramMap(): Map<String, Any?> {
            return mapOf(
                "land" to svar.alpha3Code,
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

    private class DokumentBuilder(svar: Dokument, private val besvartAv: Int?) : UpdateClauseBuilder {
        private lateinit var opplastet: LocalDateTime
        private lateinit var urn: String

        init {
            svar.reflection { l, u: String ->
                opplastet = l
                urn = u
            }
        }

        @Language("PostgreSQL")
        override fun updateClause() =
            """UPDATE faktum_verdi SET besvart_av = $besvartAv , opprettet=NOW() AT TIME ZONE 'utc'"""

        override fun insertStmt() = // language=PostgreSQL
            """INSERT INTO dokument (faktum_verdi_id, opplastet, urn) VALUES ((SELECT id FROM oppdatert_faktum_verdi_id), :opplastet, :urn) 
               ON CONFLICT (faktum_verdi_id) DO UPDATE SET opplastet = :opplastet, urn = :urn
            """.trimMargin()

        override fun paramMap(): Map<String, Any?> {
            return mapOf(
                "opplastet" to Parameter(opplastet, LocalDateTime::class.java),
                "urn" to Parameter(urn, String::class.java),
            )
        }
    }

    private object NullFaktumBuilder : UpdateClauseBuilder {
        @Language("PostgreSQL")
        override fun updateClause() =
            """UPDATE faktum_verdi
SET boolsk         = NULL,
    dato           = NULL,
    aarlig_inntekt = NULL,
    heltall        = NULL,
    desimaltall    = NULL,
    envalg_id      = NULL,
    flervalg_id    = NULL,
    periode_id     = NULL,
    land           = NULL,
    tekst          = NULL,
    dokument_id    = NULL,
    opprettet=NOW() AT TIME ZONE 'utc' """
    }
}
