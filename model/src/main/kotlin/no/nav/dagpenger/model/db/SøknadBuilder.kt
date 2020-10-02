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
import no.nav.dagpenger.model.søknad.Seksjon
import no.nav.dagpenger.model.søknad.Søknad
import java.time.LocalDate
import java.util.UUID
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.isAccessible

class SøknadBuilder(private val jsonString: String) {
    private lateinit var søknad: Søknad

    val mapper = ObjectMapper()
    val json = mapper.readTree(jsonString)
    val fakta = mutableMapOf<String, Faktum<*>>()

    fun resultat(): Søknad {
        byggFakta(json["fakta"])
        val uuid = UUID.fromString(json["root"]["uuid"].asText())
        val seksjoner = json["root"]["seksjoner"].mapNotNull { seksjon -> byggSeksjon(seksjon) }.toMutableList()
        return Søknad::class.primaryConstructor!!.apply { isAccessible = true }.call(uuid, seksjoner)
    }

    private fun byggFakta(faktaNode: JsonNode) {
        faktaNode.forEach { faktumNode ->
            byggFaktum(faktumNode)
        }
    }

    private fun byggFaktum(faktumNode: JsonNode) {
        val navn = faktumNode["navn"].asText()
        val id = faktumNode["id"].asText()
        val rootId = faktumNode["rootId"].asInt()
        val indeks = faktumNode["indeks"].asInt()
        val clazz = faktumNode["clazz"].asText()
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

    private fun byggSeksjon(seksjonJson: JsonNode): Seksjon {
        val fakta = seksjonJson["fakta"].mapNotNull { fakta[it.asText()] }.toTypedArray()
        val rolle = Rolle.valueOf(seksjonJson["rolle"].asText())
        return Seksjon(rolle, *fakta)
    }
}
