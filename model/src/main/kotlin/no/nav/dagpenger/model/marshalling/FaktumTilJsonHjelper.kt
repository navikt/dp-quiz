package no.nav.dagpenger.model.marshalling

import com.fasterxml.jackson.annotation.JsonInclude
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
import no.nav.dagpenger.model.faktum.LandGrupper
import no.nav.dagpenger.model.faktum.Periode
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Tekst
import java.time.LocalDate

object FaktumTilJsonHjelper {
    private val mapper =
        ObjectMapper().also {
            it.setSerializationInclusion(JsonInclude.Include.NON_NULL)
        }

    internal fun <R> ObjectNode.putR(
        beskrivendeId: String = "svar",
        svar: R,
    ) {
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
            mapper.createObjectNode().also { node ->
                node.put("fom", fom.toString())
                tom?.let { node.put("tom", tom.toString()) }
            }
        }

    private fun Flervalg.asJsonNode(): ArrayNode? {
        val flervalg = mapper.createArrayNode()
        forEach { flervalg.add(it) }
        return flervalg
    }

    private fun Inntekt.asJsonNode() = reflection { årlig, _, _, _ -> årlig }

    internal fun <Boolean : Comparable<Boolean>> Faktum<Boolean>.lagBeskrivendeIderForGyldigeBoolskeValg() = GyldigeValg("$navn.svar.ja", "$navn.svar.nei")

    internal fun Class<*>.erBoolean() = this.isAssignableFrom(Boolean::class.java)

    internal fun Class<*>.erLand() = this.isAssignableFrom(Land::class.java)

    internal fun <R : Comparable<R>> ObjectNode.lagFaktumNode(
        id: String,
        clazz: String,
        navn: String? = null,
        roller: Set<Rolle>,
        templates: ArrayNode? = null,
        gyldigeValg: GyldigeValg? = null,
        svar: R? = null,
        besvartAv: String? = null,
    ) {
        this.also { faktumNode ->
            faktumNode.put("id", id)
            faktumNode.put("type", clazz)
            faktumNode.put("beskrivendeId", navn)
            svar?.also { faktumNode.putR("svar", it) }
            besvartAv?.also { faktumNode.put("besvartAv", it) }
            faktumNode.putArray("roller").also { arrayNode ->
                roller.forEach { rolle ->
                    arrayNode.add(rolle.typeNavn)
                }
            }
            gyldigeValg?.let { gv ->
                faktumNode.putArray("gyldigeValg").also { arrayNode ->
                    gv.forEach {
                        arrayNode.add(it)
                    }
                }
            }
            if (templates != null) faktumNode.set<ArrayNode>("templates", templates)
        }
    }

    internal fun ObjectNode.leggTilLandGrupper(landGrupper: LandGrupper?) {
        this.putArray("grupper").also { landgrupperNode ->
            landGrupper?.forEach { (gruppe, land) ->
                val gruppeNode = mapper.createObjectNode()
                gruppeNode.put("gruppeId", gruppe)
                val landNode =
                    land.foldRight(mapper.createArrayNode()) { landkode, array -> array.also { it.add(landkode.alpha3Code) } }
                gruppeNode.set<ArrayNode>("land", landNode)
                landgrupperNode.add(gruppeNode)
            }
        }
    }

    internal fun ObjectNode.leggTilGyldigeLand(landGrupper: LandGrupper?) {
        this.putArray("gyldigeLand").addAll(
            landGrupper?.flatMap { (_, land) -> land }?.toSet()?.map { this.textNode(it.alpha3Code) },
        )
    }
}
