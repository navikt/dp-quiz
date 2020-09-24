package no.nav.dagpenger.model.visitor

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.ObjectWriter
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import no.nav.dagpenger.model.fakta.Dokument
import no.nav.dagpenger.model.fakta.Faktum
import no.nav.dagpenger.model.fakta.GrunnleggendeFaktum
import no.nav.dagpenger.model.fakta.Rolle
import no.nav.dagpenger.model.fakta.UtledetFaktum
import java.time.LocalDate

abstract class FaktumJsonBuilder : FaktumVisitor {
    protected val mapper = ObjectMapper()
    protected var root: ObjectNode = mapper.createObjectNode()
    protected val arrayNodes: MutableList<ArrayNode> = mutableListOf(mapper.createArrayNode())
    protected val objectNodes: MutableList<ObjectNode> = mutableListOf()

    private val faktaNode = mapper.createArrayNode()
    private val faktumIder = mutableSetOf<Int>()

    fun resultat(): ObjectNode = mapper.createObjectNode().also {
        it.set("fakta", faktaNode)
        it.set("root", root)
    }

    override fun toString(): String =
        ObjectMapper().writerWithDefaultPrettyPrinter<ObjectWriter>().writeValueAsString(resultat())

    override fun <R : Comparable<R>> preVisit(
        faktum: UtledetFaktum<R>,
        id: Int,
        avhengigeFakta: Set<Faktum<*>>,
        children: Set<Faktum<*>>,
        clazz: Class<R>,
        svar: R
    ) {
        if (id in faktumIder) return
        faktumNode(faktum, id, avhengigeFakta, clazz).also { faktumNode ->
            faktumNode.set("fakta", mapper.valueToTree(children.map { it.id }))
            faktumNode.putR(svar)
        }
        faktumIder.add(id)
    }

    override fun <R : Comparable<R>> preVisit(
        faktum: UtledetFaktum<R>,
        id: Int,
        avhengigeFakta: Set<Faktum<*>>,
        children: Set<Faktum<*>>,
        clazz: Class<R>
    ) {
        if (id in faktumIder) return
        faktumNode(faktum, id, avhengigeFakta, clazz).also { faktumNode ->
            faktumNode.set("fakta", mapper.valueToTree(children.map { it.id }))
        }
        faktumIder.add(id)
    }

    override fun <R : Comparable<R>> visit(
        faktum: GrunnleggendeFaktum<R>,
        tilstand: Faktum.FaktumTilstand,
        id: Int,
        avhengigeFakta: Set<Faktum<*>>,
        roller: Set<Rolle>,
        clazz: Class<R>
    ) {
        if (id in faktumIder) return
        faktumNode(faktum, id, avhengigeFakta, clazz).also { faktumNode ->
            faktumNode.set("roller", mapper.valueToTree(roller.map { it.name }))
        }
        faktumIder.add(id)
    }

    override fun <R : Comparable<R>> visit(
        faktum: GrunnleggendeFaktum<R>,
        tilstand: Faktum.FaktumTilstand,
        id: Int,
        avhengigeFakta: Set<Faktum<*>>,
        roller: Set<Rolle>,
        clazz: Class<R>,
        svar: R
    ) {
        if (id in faktumIder) return
        faktumNode(faktum, id, avhengigeFakta, clazz).also { faktumNode ->
            faktumNode.set("roller", mapper.valueToTree(roller.map { it.name }))
            faktumNode.putR(svar)
        }
        faktumIder.add(id)
    }

    private fun <R : Comparable<R>> faktumNode(
        faktum: Faktum<R>,
        id: Int,
        avhengigeFakta: Set<Faktum<*>>,
        clazz: Class<R>
    ) =
        mapper.createObjectNode().also { faktumNode ->
            faktumNode.put("navn", faktum.navn.toString())
            faktumNode.put("id", id)
            faktumNode.set("avhengigFakta", mapper.valueToTree(avhengigeFakta.map { it.id }))
            faktumNode.put("clazz", clazz.simpleName.toLowerCase())
            faktaNode.add(faktumNode)
        }

    private fun <R : Comparable<R>> ObjectNode.putR(svar: R) {
        when (svar) {
            is Boolean -> this.put("svar", svar)
            is Int -> this.put("svar", svar)
            is Double -> this.put("svar", svar)
            is String -> this.put("svar", svar)
            is LocalDate -> this.put("svar", svar.toString())
            is Dokument -> this.put("svar", svar.toUrl())
            else -> throw IllegalArgumentException("Ukjent datatype")
        }
    }
}
