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
            "boolean" -> lagBooleanFaktum(beskrivendeId)
            "valg" -> lagEnvalgFaktum(beskrivendeId, faktum)
            "flervalg" -> lagFlervalgFaktum(beskrivendeId, faktum)
        }
    }

    private fun lagBooleanFaktum(beskrivendeId: String) {
        val databaseId = lagDatabaseId(beskrivendeId)
        dslResultat.append(
            """boolsk faktum "$beskrivendeId" id `$databaseId`,
            |
            """.trimMargin()
        )
    }

    private fun lagDatabaseId(beskrivendeId: String) = beskrivendeId.replace("faktum.", "") // + " databaseId"

    private fun lagEnvalgFaktum(beskrivendeId: String, faktum: JsonNode) {
        val databaseId = lagDatabaseId(beskrivendeId)
        val valgSomDsl = lagValgalternativer(faktum, beskrivendeId)
        dslResultat.append(
            """envalg faktum "$beskrivendeId"
            | $valgSomDsl id `$databaseId`,
            |
            """.trimMargin()
        )
    }

    private fun lagFlervalgFaktum(beskrivendeId: String, faktum: JsonNode) {
        val databaseId = lagDatabaseId(beskrivendeId)
        val valgSomDsl = lagValgalternativer(faktum, beskrivendeId)
        dslResultat.append(
            """flervalg faktum "$beskrivendeId"
            | $valgSomDsl id `$databaseId`,
            |
            """.trimMargin()
        )
    }

    private fun lagValgalternativer(faktum: JsonNode, beskrivendeId: String): String {
        val valgAlternativer = faktum["answerOptions"].map { faktumNode ->
            faktumNode["id"].asText().replace("$beskrivendeId.", "")
        }
        return " med \"" + valgAlternativer.joinToString("\"\n  med \"") + "\""
    }
}
