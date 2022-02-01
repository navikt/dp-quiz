package no.nav.dagpenger.quiz.mediator.helpers

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue

class BffTilDslGenerator(bffJson: String) {

    private val objectmapper = jacksonObjectMapper()
    private val seksjonJsonNode = objectmapper.readValue<JsonNode>(bffJson)
    private val fakta = seksjonJsonNode["faktum"]

    private val dslResultat = StringBuilder()

    init {
        generer()
    }

    private fun generer() {
        fakta.forEach { faktum ->
            val beskrivendeId = faktum["id"].asText()
            val type = faktum["type"].asText()
            lagDSLFaktum(beskrivendeId, type, faktum)
        }
    }

    override fun toString(): String {
        return dslResultat.toString()
    }

    private fun lagDSLFaktum(beskrivendeId: String, type: String, faktum: JsonNode) {
        when (type) {
            "boolean" -> lagBooleanDslFaktum(beskrivendeId)
            "valg" -> lagEnvalgDslFaktum(beskrivendeId, faktum)
        }
    }

    private fun lagBooleanDslFaktum(beskrivendeId: String) {
        val databaseId = beskrivendeId.replace("faktum.", "") // + " databaseId"
        dslResultat.append(
            """boolsk faktum "$beskrivendeId" id `$databaseId`,
            |
            """.trimMargin()
        )
    }

    private fun lagEnvalgDslFaktum(beskrivendeId: String, faktum: JsonNode) {
        val databaseId = beskrivendeId.replace("faktum.", "") // + " databaseId"
        val valgAlternativer = faktum["answerOptions"].map { faktumNode ->
            faktumNode["id"].asText().replace("$beskrivendeId.", "")
        }

        val valgSomDsl = "med \"" + valgAlternativer.joinToString("\"\n med \"") + "\""

        dslResultat.append(
            """envalg faktum "$beskrivendeId"
            | $valgSomDsl id `$databaseId`,
            """.trimMargin()
        )
    }
}
