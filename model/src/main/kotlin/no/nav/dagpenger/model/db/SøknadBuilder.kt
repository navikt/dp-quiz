package no.nav.dagpenger.model.db

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.dagpenger.model.fakta.Faktum
import no.nav.dagpenger.model.fakta.FaktumNavn
import no.nav.dagpenger.model.fakta.Rolle
import no.nav.dagpenger.model.fakta.faktum
import no.nav.dagpenger.model.søknad.Seksjon
import no.nav.dagpenger.model.søknad.Søknad
import java.util.UUID

class SøknadBuilder(private val jsonString: String) {
    private lateinit var søknad: Søknad

    val mapper = ObjectMapper()
    val json = mapper.readTree(jsonString)
    val fakta = mutableMapOf<String, Faktum<*>>()

    fun resultat(): Søknad {
        byggFakta(json["fakta"])
        val uuid = UUID.fromString(json["root"]["uuid"].asText())
        val seksjoner = json["root"]["seksjoner"].mapNotNull { seksjon -> byggSeksjon(seksjon) }.toTypedArray()
        return Søknad(*seksjoner)
    }

    private fun byggFakta(faktaNode: JsonNode) {
        faktaNode.forEach { faktumNode ->
            byggFaktum(faktumNode)
        }
    }

    private fun byggFaktum(faktumNode: JsonNode) {
        val navn = faktumNode["navn"].asText()
        val id = faktumNode["id"].asText()
        val roller = faktumNode["roller"].mapNotNull { rolleNode -> Rolle.valueOf(rolleNode.asText()) }
        fakta[id] = FaktumNavn(id.toInt(), navn).faktum(Boolean::class.java)
    }

    private fun byggSeksjon(seksjonJson: JsonNode): Seksjon {
        val fakta = seksjonJson["fakta"].mapNotNull { fakta[it.asText()] }.toTypedArray()
        return Seksjon(Rolle.søker, *fakta)
    }
}
