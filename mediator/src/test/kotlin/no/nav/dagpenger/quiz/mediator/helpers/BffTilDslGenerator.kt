package no.nav.dagpenger.quiz.mediator.helpers

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.json.JsonReadFeature
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonMapperBuilder
import com.fasterxml.jackson.module.kotlin.readValue

class BffTilDslGenerator(bffJson: String) {

    private val objectMapper = configureLiberalObjectMapper()
    private val seksjonJsonNode = objectMapper.readValue<JsonNode>(bffJson)
    private val fakta = seksjonJsonNode["faktum"]

    private val dslResultat = StringBuilder()

    init {
        generer()
    }

    private fun generer() {
        val antallFakta = fakta.size()
        fakta.forEachIndexed { index, faktum ->
            val type = faktum["type"].asText()
            val beskrivendeId = faktum["id"].asText()
            val dslFaktum = lagDSLFaktum(beskrivendeId, type, faktum)

            dslResultat.append(dslFaktum)
                .append(skilletegnHvisFlereElementer(index, antallFakta))
        }
    }

    override fun toString(): String {
        return dslResultat.toString()
    }

    private fun lagDSLFaktum(beskrivendeId: String, type: String, faktum: JsonNode): String {
        return when (type) {
            "valg", "dropdown" -> lagEnvalgFaktum(beskrivendeId, faktum)
            "flervalg" -> lagFlervalgFaktum(beskrivendeId, faktum)
            "generator" -> lagGeneratorFaktum(beskrivendeId, faktum)
            else -> lagFaktum(type, beskrivendeId)
        }
    }

    private fun lagFaktum(type: String, beskrivendeId: String): String {
        val faktumtype = oversettTilDslType(type)
        val databaseId = lagDatabaseId(beskrivendeId)

        return """$faktumtype faktum "$beskrivendeId" id $databaseId"""
    }

    private fun oversettTilDslType(bffType: String): String = when (bffType) {
        "int" -> "heltall"
        "tekst" -> "tekst"
        "double" -> "desimaltall"
        "periode" -> "periode"
        "boolean" -> "boolsk"
        "localdate" -> "dato"
        else -> throw IllegalArgumentException("Ukjent faktumtype $bffType")
    }

    private fun lagDatabaseId(beskrivendeId: String): String {
        val idUtenPrefix = beskrivendeId.replace("faktum.", "") // + " databaseId"
        return "`$idUtenPrefix`"
    }

    private fun lagEnvalgFaktum(beskrivendeId: String, faktum: JsonNode): String {
        val databaseId = lagDatabaseId(beskrivendeId)
        val valgSomDsl = lagValgalternativer(faktum, beskrivendeId)

        return """envalg faktum "$beskrivendeId"
            | $valgSomDsl id $databaseId""".trimMargin()
    }

    private fun lagFlervalgFaktum(beskrivendeId: String, faktum: JsonNode): String {
        val databaseId = lagDatabaseId(beskrivendeId)
        val valgSomDsl = lagValgalternativer(faktum, beskrivendeId)

        return """flervalg faktum "$beskrivendeId"
            | $valgSomDsl id $databaseId""".trimMargin()
    }

    private fun lagValgalternativer(faktum: JsonNode, beskrivendeId: String): String {
        val valgAlternativer = faktum["answerOptions"].map { faktumNode ->
            faktumNode["id"].asText().replace("$beskrivendeId.", "")
        }
        return " med \"" + valgAlternativer.joinToString("\"\n  med \"") + "\""
    }

    private fun lagGeneratorFaktum(beskrivendeId: String, generatorFaktumNode: JsonNode): String {
        val generatorGrunnfaktumDSL = lagDSLFaktum(beskrivendeId, "int", generatorFaktumNode)
        val faktaSomSkalGenereres = generatorFaktumNode["faktum"]
        val iderTilGenererteFakta = byggIdlisteOverFaktaSomSkalGenereres(faktaSomSkalGenereres)
        val genererteFakta = byggFaktaSomSkalGenereres(faktaSomSkalGenereres)
        return """$generatorGrunnfaktumDSL
                  |  genererer $iderTilGenererteFakta,
                  |$genererteFakta""".trimMargin()
    }

    private fun byggIdlisteOverFaktaSomSkalGenereres(faktaSomSkalGenereres: JsonNode): String {
        val iderForFaktaSomSkalGenereres = faktaSomSkalGenereres.map { faktum ->
            val beskrivendeId = faktum["id"].asText()
            lagDatabaseId(beskrivendeId)
        }

        return iderForFaktaSomSkalGenereres.joinToString("\n  og ")
    }

    private fun byggFaktaSomSkalGenereres(faktaSomSkalGenereres: JsonNode): StringBuilder {
        val faktaDsl = StringBuilder()
        val antallFakta = faktaSomSkalGenereres.size()
        faktaSomSkalGenereres.forEachIndexed { index, faktum ->
            val type = faktum["type"].asText()
            val beskrivendeId = faktum["id"].asText()
            faktaDsl.append(
                lagDSLFaktum(beskrivendeId, type, faktum)
            ).append(skilletegnHvisFlereElementer(index, antallFakta))
        }
        return faktaDsl
    }

    private fun skilletegnHvisFlereElementer(index: Int, antallFakta: Int, skilletegn: String = ",\n") =
        if (index == antallFakta - 1) "" else skilletegn

    private fun configureLiberalObjectMapper() = jacksonMapperBuilder()
        .enable(JsonParser.Feature.ALLOW_COMMENTS)
        .enable(JsonReadFeature.ALLOW_TRAILING_COMMA)
        .enable(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES)
        .build()
}
