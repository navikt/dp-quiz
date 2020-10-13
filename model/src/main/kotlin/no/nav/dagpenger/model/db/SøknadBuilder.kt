package no.nav.dagpenger.model.db

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.dagpenger.model.fakta.Dokument
import no.nav.dagpenger.model.fakta.Faktum
import no.nav.dagpenger.model.fakta.FaktumNavn
import no.nav.dagpenger.model.fakta.Inntekt
import no.nav.dagpenger.model.fakta.Inntekt.Companion.årlig
import no.nav.dagpenger.model.fakta.Rolle
import no.nav.dagpenger.model.fakta.TemplateFaktum
import no.nav.dagpenger.model.fakta.faktum
import no.nav.dagpenger.model.fakta.template
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
    private val generatorFaktumNoder = mutableListOf<JsonNode>()
    private val faktaIder = mutableMapOf<Faktum<*>, List<String>>()

    fun resultat(): Søknad {
        byggGrunnleggendeFakta()
        byggUtledetFakta()
        byggGeneratorFakta()
        byggAvhengigheter()
        val uuid = UUID.fromString(json["root"]["uuid"].asText())
        val seksjoner = json["root"]["seksjoner"].mapNotNull { seksjon -> byggSeksjon(seksjon) }.toMutableList()
        return Søknad::class.primaryConstructor!!.apply { isAccessible = true }.call(uuid, seksjoner)
    }

    private fun byggGeneratorFakta() {
        generatorFaktumNoder.forEach {
            byggGeneratorFaktum(it)
        }
    }

    private fun byggGeneratorFaktum(faktumNode: JsonNode) {
        parametere(faktumNode) { navn: String, id: String, rootId: Int, indeks: Int, clazz: String, ider: List<String> ->
            val roller = faktumNode["roller"].mapNotNull { Rolle.valueOf(it.asText()) }
            fakta[id] =
                (faktumNavn(rootId, navn, indeks, clazz) as FaktumNavn<Int>).faktum(
                    templates = *(faktumNode["templates"].map {
                        fakta[it.asText()] as TemplateFaktum<*>
                    }.toTypedArray()
                        )
                ).also { faktum ->
                    roller.forEach { faktum.add(it) }
                    if (faktumNode.has("svar"))
                        (faktum as Faktum<Int>).besvar(faktumNode["svar"].asInt(), roller.first())
                    faktaIder[faktum] = ider
                }
        }
    }

    private fun parametere(faktumNode: JsonNode, block: (String, String, Int, Int, String, List<String>) -> Unit) =
        block(
            faktumNode["navn"].asText(),
            faktumNode["id"].asText(),
            faktumNode["rootId"].asInt(),
            faktumNode["indeks"].asInt(),
            faktumNode["clazz"].asText(),
            faktumNode["avhengigFakta"].map(JsonNode::asText)
        )

    private fun byggUtledetFakta() {
        while (utledetFaktumNoder.isNotEmpty()) {
            byggUtledetFakta(
                utledetFaktumNoder.toList().also {
                    utledetFaktumNoder.clear()
                }
            )
        }
    }

    private fun byggAvhengigheter() {
        faktaIder.forEach { (faktum, ider) ->
            ider.forEach { id ->
                fakta[id]?.avhengerAv(faktum) ?: throw IllegalArgumentException("Mangler faktum med id: $id")
            }
        }
    }

    private fun byggUtledetFakta(faktumNoder: List<JsonNode>) {
        faktumNoder.forEach { faktumNode ->
            byggUtledetFaktum(faktumNode)
        }
    }

    private fun byggGrunnleggendeFakta() {
        json["fakta"].forEach { faktumNode ->
            when {
                faktumNode.has("templates") -> generatorFaktumNoder.add(faktumNode)
                faktumNode.has("fakta") -> utledetFaktumNoder.add(faktumNode)
                else -> byggGrunnleggendeFaktum(faktumNode)
            }
        }
    }

    private fun byggUtledetFaktum(faktumNode: JsonNode) {
        parametere(faktumNode) { navn: String, id: String, rootId: Int, indeks: Int, clazz: String, ider: List<String> ->
            if (faktumNode["fakta"].any { this.fakta[it.asText()] == null }) {
                utledetFaktumNoder.add(faktumNode)
            } else {
                val type = FaktumNavn<LocalDate>("1")
                val fakta: List<Faktum<LocalDate>> =
                    faktumNode["fakta"].mapNotNull { this.fakta[it.asText()] as Faktum<LocalDate> }
                this.fakta[id] = fakta.faktum(
                    type::class.primaryConstructor!!.apply { isAccessible = true }
                        .call(rootId, navn, indeks), MAKS_DATO)
                    .also { faktum ->
                        faktaIder[faktum] = ider
                    }
            }
        }
    }

    private fun byggGrunnleggendeFaktum(faktumNode: JsonNode) {
        parametere(faktumNode) { navn: String, id: String, rootId: Int, indeks: Int, clazz: String, ider: List<String> ->
            val roller = faktumNode["roller"].mapNotNull { Rolle.valueOf(it.asText()) }

            val faktaNavn = faktumNavn(rootId, navn, indeks, clazz)
            fakta[id] = asTypedFaktum(faktaNavn, clazz, faktumNode["type"].asText())
                .also { faktum ->
                    faktaIder[faktum] = ider
                    roller.forEach { faktum.add(it) }
                    if (faktumNode.has("svar")) when (clazz) {
                        "boolean" -> (faktum as Faktum<Boolean>).besvar(faktumNode["svar"].asBoolean(), roller.first())
                        "int" -> (faktum as Faktum<Int>).besvar(faktumNode["svar"].asInt(), roller.first())
                        "inntekt" -> (faktum as Faktum<Inntekt>).besvar(
                            faktumNode["svar"].asDouble().årlig,
                            roller.first()
                        )
                        "localdate" -> (faktum as Faktum<LocalDate>).besvar(
                            LocalDate.parse(faktumNode["svar"].asText()),
                            roller.first()
                        )
                        "dokument" -> (faktum as Faktum<Dokument>).besvar(
                            Dokument(LocalDate.parse(faktumNode["svar"]["opplastingsdato"].asText())),
                            roller.first()
                        )
                    }
                }
        }
    }

    private fun faktumNavn(
        rootId: Int,
        navn: String,
        indeks: Int,
        clazz: String
    ): FaktumNavn<out Comparable<*>> {
        val type = when (clazz) {
            "boolean" -> FaktumNavn<Boolean>("1")
            "int" -> FaktumNavn<Int>("1")
            "integer" -> FaktumNavn<Int>("1")
            "inntekt" -> FaktumNavn<Inntekt>("1")
            "localdate" -> FaktumNavn<LocalDate>("1")
            "dokument" -> FaktumNavn<Dokument>("1")
            else -> throw IllegalArgumentException("Kjenner ikke clazz $clazz")
        }

        return type::class.primaryConstructor!!.apply { isAccessible = true }.call(rootId, navn, indeks)
    }

    private inline fun asTypedFaktum(faktumNavn: FaktumNavn<*>, clazz: String, type: String): Faktum<*> {
        return if (type == TemplateFaktum::class.java.simpleName) {
            when (clazz) {
                "boolean" -> (faktumNavn as FaktumNavn<Boolean>).template()
                "dokument" -> (faktumNavn as FaktumNavn<Dokument>).template()
                "inntekt" -> (faktumNavn as FaktumNavn<Inntekt>).template()
                "int" -> (faktumNavn as FaktumNavn<Int>).template()
                "integer" -> (faktumNavn as FaktumNavn<Int>).template()
                "localdate" -> (faktumNavn as FaktumNavn<LocalDate>).template()
                else -> throw IllegalArgumentException("Kjenner ikke clazz $clazz")
            }
        } else {
            when (clazz) {
                "boolean" -> (faktumNavn as FaktumNavn<Boolean>).faktum()
                "dokument" -> (faktumNavn as FaktumNavn<Dokument>).faktum()
                "inntekt" -> (faktumNavn as FaktumNavn<Inntekt>).faktum()
                "int" -> (faktumNavn as FaktumNavn<Int>).faktum()
                "integer" -> (faktumNavn as FaktumNavn<Int>).faktum()
                "localdate" -> (faktumNavn as FaktumNavn<LocalDate>).faktum()
                else -> throw IllegalArgumentException("Kjenner ikke clazz $clazz")
            }
        }
    }

    private fun byggSeksjon(seksjonJson: JsonNode): Seksjon {
        val fakta = seksjonJson["fakta"].mapNotNull { fakta[it.asText()] }.toTypedArray()
        val navn = seksjonJson["navn"].asText()
        val rolle = Rolle.valueOf(seksjonJson["rolle"].asText())
        return Seksjon(navn, rolle, *fakta)
    }
}
