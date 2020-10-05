package no.nav.dagpenger.model.db

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.dagpenger.model.fakta.Dokument
import no.nav.dagpenger.model.fakta.Faktum
import no.nav.dagpenger.model.fakta.FaktumNavn
import no.nav.dagpenger.model.fakta.Inntekt
import no.nav.dagpenger.model.fakta.Inntekt.Companion.årlig
import no.nav.dagpenger.model.fakta.Rolle
import no.nav.dagpenger.model.fakta.faktum
import no.nav.dagpenger.model.regel.MAKS_DATO
import no.nav.dagpenger.model.søknad.Seksjon
import no.nav.dagpenger.model.søknad.Søknad
import java.time.LocalDate
import java.util.UUID
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.isAccessible

class SøknadBuilder(private val jsonString: String) {
    private lateinit var søknad: Søknad

    private val mapper = ObjectMapper()
    private val json = mapper.readTree(jsonString)
    private val fakta = mutableMapOf<String, Faktum<*>>()
    private val utledetFaktumNoder = mutableListOf<JsonNode>()

    fun resultat(): Søknad {
        byggFakta(json["fakta"])
        byggUtledetFakta()
        val uuid = UUID.fromString(json["root"]["uuid"].asText())
        val seksjoner = json["root"]["seksjoner"].mapNotNull { seksjon -> byggSeksjon(seksjon) }.toMutableList()
        return Søknad::class.primaryConstructor!!.apply { isAccessible = true }.call(uuid, seksjoner)
    }

    private fun parametere(faktumNode: JsonNode, block: (String, String, Int, Int, String) -> Unit) = block(
        faktumNode["navn"].asText(),
        faktumNode["id"].asText(),
        faktumNode["rootId"].asInt(),
        faktumNode["indeks"].asInt(),
        faktumNode["clazz"].asText()
    )

    private fun byggUtledetFakta() {
        utledetFaktumNoder.forEach { faktumNode ->
            byggUtledetFaktum(faktumNode)
        }
    }

    private fun byggFakta(faktaNode: JsonNode) {
        faktaNode.forEach { faktumNode ->
            byggFaktum(faktumNode)
        }
    }

    private fun byggFaktum(faktumNode: JsonNode) {
        if (faktumNode.has("fakta")) byggUtledetFaktum(faktumNode)
        else byggGrunnleggendeFaktum(faktumNode)
    }

    private fun byggUtledetFaktum(faktumNode: JsonNode) {
        parametere(faktumNode) { navn: String, id: String, rootId: Int, indeks: Int, clazz: String ->
            if (faktumNode["fakta"].any { this.fakta[it.asText()] == null }) {
                utledetFaktumNoder.add(faktumNode)
            } else {
                val fakta: List<Faktum<LocalDate>> = faktumNode["fakta"].mapNotNull { this.fakta[it.asText()] as Faktum<LocalDate> }
                this.fakta[id] = fakta.faktum(FaktumNavn::class.primaryConstructor!!.apply { isAccessible = true }.call(rootId, navn, indeks), MAKS_DATO)
            }
        }
    }

    private fun byggGrunnleggendeFaktum(faktumNode: JsonNode) {
        parametere(faktumNode) { navn: String, id: String, rootId: Int, indeks: Int, clazz: String ->
            val roller = faktumNode["roller"].mapNotNull { Rolle.valueOf(it.asText()) }
            fakta[id] = FaktumNavn::class.primaryConstructor!!.apply { isAccessible = true }.call(rootId, navn, indeks).faktum(
                when (clazz) {
                    "boolean" -> Boolean::class.java
                    "int" -> Int::class.java
                    "inntekt" -> Inntekt::class.java
                    "localdate" -> LocalDate::class.java
                    "dokument" -> Dokument::class.java
                    else -> throw IllegalArgumentException("Kjenner ikke clazz $clazz")
                }
            ).also { faktum ->
                roller.forEach { faktum.add(it) }
                if (faktumNode.has("svar")) when (clazz) {
                    "boolean" -> (faktum as Faktum<Boolean>).besvar(faktumNode["svar"].asBoolean(), roller.first())
                    "int" -> (faktum as Faktum<Int>).besvar(faktumNode["svar"].asInt(), roller.first())
                    "inntekt" -> (faktum as Faktum<Inntekt>).besvar(faktumNode["svar"].asDouble().årlig, roller.first())
                    "localdate" -> (faktum as Faktum<LocalDate>).besvar(LocalDate.parse(faktumNode["svar"].asText()), roller.first())
                    "dokument" -> (faktum as Faktum<Dokument>).besvar(Dokument(LocalDate.parse(faktumNode["svar"]["opplastingsdato"].asText())), roller.first())
                }
            }
        }
    }

    private fun byggSeksjon(seksjonJson: JsonNode): Seksjon {
        val fakta = seksjonJson["fakta"].mapNotNull { fakta[it.asText()] }.toTypedArray()
        val rolle = Rolle.valueOf(seksjonJson["rolle"].asText())
        return Seksjon(rolle, *fakta)
    }
}
