package no.nav.dagpenger.quiz.mediator.helpers

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.json.JsonReadFeature
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonMapperBuilder
import com.fasterxml.jackson.module.kotlin.readValue

class BffTilDslGenerator(bffJson: String) {

    private val objectMapper = configureLiberalObjectMapper()
    private val seksjonJsonNode = objectMapper.readValue<JsonNode>(bffJson)
    private val faktaNode = seksjonJsonNode["faktum"]
    private var idTeller = 1
    private val databaseIder = mutableMapOf<String, Int>()

    private val komplettDslResultat = StringBuilder()
    private var variabler = ""

    init {
        generer()
    }

    private fun generer() {
        genererAlleFakta()
        genererVariabelseksjon()
    }

    private fun genererAlleFakta() {
        val antallFakta = faktaNode.size()
        faktaNode.forEachIndexed { index, faktumNode ->
            val dslFaktum = faktumNode.genererDslFaktum()
            komplettDslResultat
                .append(dslFaktum)
                .append(skilletegnHvisFlereElementer(index, antallFakta))
        }
    }

    private fun genererVariabelseksjon() {
        variabler = databaseIder.map { databaseId ->
            "const val ${databaseId.key} = ${databaseId.value}"
        }.joinToString("\n")
    }

    private fun JsonNode.genererDslFaktum(): String {
        return when (faktumtype()) {
            "valg", "dropdown" -> lagEnvalgFaktum()
            "flervalg" -> lagFlervalgFaktum()
            "generator" -> lagGeneratorFaktum()
            else -> lagBasisFaktum()
        }
    }

    private fun JsonNode.faktumtype() = this["type"].asText()
    private fun JsonNode.beskrivendeId() = this["id"].asText()

    private fun JsonNode.lagBasisFaktum(): String {
        val faktumtype = oversettTilDslType(faktumtype())
        val databaseId = lagDatabaseId()
        return """$faktumtype faktum "${beskrivendeId()}" id $databaseId"""
    }

    private fun oversettTilDslType(bffType: String): String = when (bffType) {
        "int", "generator" -> "heltall"
        "tekst" -> "tekst"
        "double" -> "desimaltall"
        "periode" -> "periode"
        "boolean" -> "boolsk"
        "localdate" -> "dato"
        else -> throw IllegalArgumentException("Ukjent faktumtype $bffType")
    }

    private fun JsonNode.lagDatabaseId(): String {
        val idUtenPrefix = beskrivendeId().replace("faktum.", "")
        val idMedPrefix = "`$idUtenPrefix`"

        databaseIder.computeIfAbsent(idMedPrefix) { idTeller++ }
        return idMedPrefix
    }

    private fun JsonNode.lagEnvalgFaktum(): String =
        """envalg faktum "${beskrivendeId()}"
        | ${lagValgalternativer()} id ${lagDatabaseId()}""".trimMargin()

    private fun JsonNode.lagFlervalgFaktum(): String =
        """flervalg faktum "${beskrivendeId()}"
        | ${lagValgalternativer()} id ${lagDatabaseId()}""".trimMargin()

    private fun JsonNode.lagValgalternativer(): String {
        val parentBeskrivendeId = beskrivendeId()
        val valgAlternativer = this["answerOptions"].map { faktumNode ->
            val childBeskrivendeId = faktumNode.beskrivendeId()
            childBeskrivendeId.replace("$parentBeskrivendeId.", "")
        }
        return " med \"" + valgAlternativer.joinToString("\"\n  med \"") + "\""
    }

    private fun JsonNode.lagGeneratorFaktum(): String {
        val generatorGrunnfaktum = lagBasisFaktum()
        val genererteFakta = byggGeneratorFakta()
        val iderTilGenererteFakta = byggListeOverDatabaseIder()
        return """$generatorGrunnfaktum
                  |  genererer $iderTilGenererteFakta,
                  |$genererteFakta""".trimMargin()
    }

    private fun JsonNode.byggGeneratorFakta(): StringBuilder {
        val nodensFakta = this["faktum"]
        val antallFakta = nodensFakta.size()
        val genererteFaktaDsl = StringBuilder()
        nodensFakta.forEachIndexed { index, faktum ->
            genererteFaktaDsl
                .append(faktum.genererDslFaktum())
                .append(skilletegnHvisFlereElementer(index, antallFakta))
        }
        return genererteFaktaDsl
    }

    private fun JsonNode.byggListeOverDatabaseIder(): String {
        val nodensFakta = this["faktum"]
        val alleDatabaseIder = nodensFakta.map { faktum ->
            faktum.lagDatabaseId()
        }
        return alleDatabaseIder.joinToString("\n  og ")
    }

    private fun skilletegnHvisFlereElementer(index: Int, antallFakta: Int, skilletegn: String = ",\n") =
        if (index == antallFakta - 1) "" else skilletegn

    internal fun dslResultat() = komplettDslResultat.toString()
    internal fun variabler() = variabler

    override fun toString(): String {
        return """$variabler
                 |
                 |$komplettDslResultat""".trimMargin()
    }

    private fun configureLiberalObjectMapper() = jacksonMapperBuilder()
        .enable(JsonParser.Feature.ALLOW_COMMENTS)
        .enable(JsonReadFeature.ALLOW_TRAILING_COMMA)
        .enable(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES)
        .build()
}

fun String.fjernTypescriptSyntax(): String =
    replace(Regex("import .*"), "")
        .replace(Regex("export .*"), "{")
