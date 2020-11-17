package no.nav.dagpenger.model.marshalling

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.ObjectWriter
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import no.nav.dagpenger.model.factory.FaktaRegel
import no.nav.dagpenger.model.faktum.Dokument
import no.nav.dagpenger.model.faktum.Faktum
import no.nav.dagpenger.model.faktum.FaktumId
import no.nav.dagpenger.model.faktum.GeneratorFaktum
import no.nav.dagpenger.model.faktum.GrunnleggendeFaktum
import no.nav.dagpenger.model.faktum.Inntekt
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.TemplateFaktum
import no.nav.dagpenger.model.faktum.UtledetFaktum
import no.nav.dagpenger.model.faktum.ValgFaktum
import no.nav.dagpenger.model.visitor.FaktumVisitor
import java.time.LocalDate

abstract class FaktumJsonBuilder : FaktumVisitor {
    protected val mapper = ObjectMapper()
    protected var root: ObjectNode = mapper.createObjectNode()
    protected val arrayNodes: MutableList<ArrayNode> = mutableListOf(mapper.createArrayNode())
    protected val objectNodes: MutableList<ObjectNode> = mutableListOf()

    protected val faktaNode = mapper.createArrayNode()
    private val faktumIder = mutableSetOf<String>()
    private lateinit var navn: String
    private var rootId = -1
    private var indeks = -1

    open fun resultat(): ObjectNode = mapper.createObjectNode().also {
        it.set("fakta", faktaNode)
        it.set("root", root)
    }

    override fun toString(): String =
        ObjectMapper().writerWithDefaultPrettyPrinter<ObjectWriter>().writeValueAsString(resultat())

    override fun preVisit(
        faktum: ValgFaktum,
        id: String,
        avhengigeFakta: Set<Faktum<*>>,
        avhengerAvFakta: Set<Faktum<*>>,
        underordnedeJa: Set<Faktum<Boolean>>,
        underordnedeNei: Set<Faktum<Boolean>>,
        clazz: Class<Boolean>
    ) {
        if (id in faktumIder) return
        faktumNode(faktum, id, avhengigeFakta, avhengerAvFakta, clazz).also { faktumNode ->
            faktumNode.set("faktaJa", mapper.valueToTree(underordnedeJa.map { it.id }))
            faktumNode.set("faktaNei", mapper.valueToTree(underordnedeNei.map { it.id }))
        }
        faktumIder.add(id)
    }

    override fun preVisit(
        faktum: ValgFaktum,
        id: String,
        avhengigeFakta: Set<Faktum<*>>,
        avhengerAvFakta: Set<Faktum<*>>,
        underordnedeJa: Set<Faktum<Boolean>>,
        underordnedeNei: Set<Faktum<Boolean>>,
        clazz: Class<Boolean>,
        svar: Boolean
    ) {
        if (id in faktumIder) return
        faktumNode(faktum, id, avhengigeFakta, avhengerAvFakta, clazz).also { faktumNode ->
            faktumNode.set("faktaJa", mapper.valueToTree(underordnedeJa.map { it.id }))
            faktumNode.set("faktaNei", mapper.valueToTree(underordnedeNei.map { it.id }))
            faktumNode.putR(svar)
        }
        faktumIder.add(id)
    }

    override fun <R : Comparable<R>> preVisit(
        faktum: UtledetFaktum<R>,
        id: String,
        avhengigeFakta: Set<Faktum<*>>,
        avhengerAvFakta: Set<Faktum<*>>,
        children: Set<Faktum<*>>,
        clazz: Class<R>,
        regel: FaktaRegel<R>,
        svar: R
    ) {
        if (id in faktumIder) return
        faktumNode(faktum, id, avhengigeFakta, avhengerAvFakta, clazz).also { faktumNode ->
            faktumNode.set("fakta", mapper.valueToTree(children.map { it.id }))
            faktumNode.putR(svar)
        }
        faktumIder.add(id)
    }

    override fun <R : Comparable<R>> preVisit(
        faktum: UtledetFaktum<R>,
        id: String,
        avhengigeFakta: Set<Faktum<*>>,
        avhengerAvFakta: Set<Faktum<*>>,
        children: Set<Faktum<*>>,
        clazz: Class<R>,
        regel: FaktaRegel<R>
    ) {
        if (id in faktumIder) return
        faktumNode(faktum, id, avhengigeFakta, avhengerAvFakta, clazz).also { faktumNode ->
            faktumNode.set("fakta", mapper.valueToTree(children.map { it.id }))
        }
        faktumIder.add(id)
    }

    override fun visit(faktumId: FaktumId, rootId: Int, indeks: Int) {
        this.rootId = rootId
        this.indeks = indeks
    }

    override fun <R : Comparable<R>> visit(
        faktum: GrunnleggendeFaktum<R>,
        tilstand: Faktum.FaktumTilstand,
        id: String,
        avhengigeFakta: Set<Faktum<*>>,
        avhengerAvFakta: Set<Faktum<*>>,
        roller: Set<Rolle>,
        clazz: Class<R>
    ) {
        if (id in faktumIder) return
        faktumNode(faktum, id, avhengigeFakta, avhengerAvFakta, clazz).also { faktumNode ->
            faktumNode.set("roller", mapper.valueToTree(roller.map { it.typeNavn }))
        }
        faktumIder.add(id)
    }

    override fun <R : Comparable<R>> visit(
        faktum: TemplateFaktum<R>,
        id: String,
        avhengigeFakta: Set<Faktum<*>>,
        avhengerAvFakta: Set<Faktum<*>>,
        roller: Set<Rolle>,
        clazz: Class<R>
    ) {
        if (id in faktumIder) return
        faktumNode(faktum, id, avhengigeFakta, avhengerAvFakta, clazz).also { faktumNode ->
            faktumNode.set("roller", mapper.valueToTree(roller.map { it.typeNavn }))
        }
        faktumIder.add(id)
    }

    override fun <R : Comparable<R>> visit(
        faktum: GrunnleggendeFaktum<R>,
        tilstand: Faktum.FaktumTilstand,
        id: String,
        avhengigeFakta: Set<Faktum<*>>,
        avhengerAvFakta: Set<Faktum<*>>,
        roller: Set<Rolle>,
        clazz: Class<R>,
        svar: R
    ) {
        if (id in faktumIder) return
        faktumNode(faktum, id, avhengigeFakta, avhengerAvFakta, clazz).also { faktumNode ->
            faktumNode.set("roller", mapper.valueToTree(roller.map { it.typeNavn }))
            faktumNode.putR(svar)
        }
        faktumIder.add(id)
    }

    override fun <R : Comparable<R>> visit(
        faktum: GeneratorFaktum,
        id: String,
        avhengigeFakta: Set<Faktum<*>>,
        avhengerAvFakta: Set<Faktum<*>>,
        templates: List<Faktum<*>>,
        roller: Set<Rolle>,
        clazz: Class<R>,
        svar: R
    ) {
        if (id in faktumIder) return
        faktumNode(faktum, id, avhengigeFakta, avhengerAvFakta, clazz).also { faktumNode ->
            faktumNode.set("roller", mapper.valueToTree(roller.map { it.typeNavn }))
            faktumNode.set("templates", mapper.valueToTree(templates.map { it.id }))
            faktumNode.putR(svar)
        }
        faktumIder.add(id)
    }

    override fun <R : Comparable<R>> visit(
        faktum: GeneratorFaktum,
        id: String,
        avhengigeFakta: Set<Faktum<*>>,
        avhengerAvFakta: Set<Faktum<*>>,
        templates: List<Faktum<*>>,
        roller: Set<Rolle>,
        clazz: Class<R>
    ) {
        if (id in faktumIder) return
        faktumNode(faktum, id, avhengigeFakta, avhengerAvFakta, clazz).also { faktumNode ->
            faktumNode.set("roller", mapper.valueToTree(roller.map { it.typeNavn }))
            faktumNode.set("templates", mapper.valueToTree(templates.map { it.id }))
        }
        faktumIder.add(id)
    }

    protected fun <R : Comparable<R>> faktumNode(
        faktum: Faktum<*>,
        id: String,
        avhengigeFakta: Set<Faktum<*>>,
        avhengerAvFakta: Set<Faktum<*>>,
        clazz: Class<R>
    ) =
        mapper.createObjectNode().also { faktumNode ->
            faktumNode.put("type", faktum::class.java.simpleName)
            faktumNode.put("navn", faktum.navn)
            faktumNode.put("id", id)
            faktumNode.set("avhengigFakta", mapper.valueToTree(avhengigeFakta.map { it.id }))
            faktumNode.set("avhengerAvFakta", mapper.valueToTree(avhengerAvFakta.map { it.id }))
            faktumNode.put("clazz", clazz.simpleName.toLowerCase())
            faktumNode.put("rootId", rootId)
            faktumNode.put("indeks", indeks)
            faktaNode.add(faktumNode)
        }

    private fun <R : Comparable<R>> ObjectNode.putR(svar: R) {
        when (svar) {
            is Boolean -> this.put("svar", svar)
            is Int -> this.put("svar", svar)
            is Double -> this.put("svar", svar)
            is String -> this.put("svar", svar)
            is LocalDate -> this.put("svar", svar.toString())
            is Dokument -> this.set(
                "svar",
                svar.reflection { lastOppTidsstempel, url ->
                    mapper.createObjectNode().also {
                        it.put("lastOppTidsstempel", lastOppTidsstempel.toString())
                        it.put("url", url)
                    }
                }
            )
            is Inntekt -> this.put("svar", svar.reflection { årlig, _, _, _ -> årlig })
            else -> throw IllegalArgumentException("Ukjent datatype")
        }
    }
}
