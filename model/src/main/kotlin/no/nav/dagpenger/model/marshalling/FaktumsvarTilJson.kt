package no.nav.dagpenger.model.marshalling

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import no.nav.dagpenger.model.faktum.Dokument
import no.nav.dagpenger.model.faktum.Envalg
import no.nav.dagpenger.model.faktum.Faktum
import no.nav.dagpenger.model.faktum.Flervalg
import no.nav.dagpenger.model.faktum.GyldigeValg
import no.nav.dagpenger.model.faktum.Inntekt
import no.nav.dagpenger.model.faktum.Land
import no.nav.dagpenger.model.faktum.Periode
import no.nav.dagpenger.model.faktum.Tekst
import java.time.LocalDate

object FaktumsvarTilJson {

    private val mapper = ObjectMapper()

    fun <R> ObjectNode.putR(beskrivendeId: String = "svar", svar: R) {
        when (svar) {
            is Boolean -> this.put(beskrivendeId, svar)
            is Int -> this.put(beskrivendeId, svar)
            is Double -> this.put(beskrivendeId, svar)
            is String -> this.put(beskrivendeId, svar)
            is LocalDate -> this.put(beskrivendeId, svar.toString())
            is Tekst -> this.put(beskrivendeId, svar.verdi)
            is Dokument -> this.set(beskrivendeId, svar.asJsonNode())
            is Periode -> this.set(beskrivendeId, svar.asJsonNode())
            is Flervalg -> this.set(beskrivendeId, svar.asJsonNode())
            is Envalg -> this.put(beskrivendeId, svar.first())
            is Land -> this.put(beskrivendeId, svar.alpha3Code)
            is Inntekt -> this.put(beskrivendeId, svar.asJsonNode())
            else -> throw IllegalArgumentException("Ukjent datatype ${svar!!::class.simpleName}")
        }
    }

    private fun Dokument.asJsonNode() =
        reflection { lastOppTidsstempel, urn: String ->
            mapper.createObjectNode().also {
                it.put("lastOppTidsstempel", lastOppTidsstempel.toString())
                it.put("urn", urn)
            }
        }

    private fun Periode.asJsonNode() =
        reflection { fom, tom ->
            mapper.createObjectNode().also {
                it.put("fom", fom.toString())
                it.put("tom", tom?.toString())
            }
        }

    private fun Flervalg.asJsonNode(): ArrayNode? {
        val flervalg = mapper.createArrayNode()
        forEach { flervalg.add(it) }
        return flervalg
    }

    private fun Inntekt.asJsonNode() = reflection { årlig, _, _, _ -> årlig }

    fun <Boolean : Comparable<Boolean>> Faktum<Boolean>.lagBeskrivendeIderForGyldigeBoolskeValg() =
        GyldigeValg("$navn.svar.ja", "$navn.svar.nei")

    fun Class<*>.isBoolean() = simpleName.lowercase() == "boolean"
}
