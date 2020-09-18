package no.nav.dagpenger.model.visitor

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.ObjectWriter
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import no.nav.dagpenger.model.fakta.Dokument
import no.nav.dagpenger.model.fakta.Faktum
import no.nav.dagpenger.model.fakta.GrunnleggendeFaktum
import no.nav.dagpenger.model.fakta.UtledetFaktum
import no.nav.dagpenger.model.regel.Regel
import no.nav.dagpenger.model.subsumsjon.AlleSubsumsjon
import no.nav.dagpenger.model.subsumsjon.EnkelSubsumsjon
import no.nav.dagpenger.model.subsumsjon.MinstEnAvSubsumsjon
import no.nav.dagpenger.model.subsumsjon.Subsumsjon
import java.time.LocalDate

class JsonBuilder(private val subsumsjon: Subsumsjon) : SubsumsjonVisitor {
    private val mapper = ObjectMapper()
    private var root: ObjectNode = mapper.createObjectNode()
    private val arrayNodes: MutableList<ArrayNode> = mutableListOf(mapper.createArrayNode())
    private val objectNodes: MutableList<ObjectNode> = mutableListOf()
    private val faktaNode = mapper.createArrayNode()
    private val faktumIder = mutableSetOf<Int>()
    private val subsumsjoner = mutableMapOf<Subsumsjon, ObjectNode>()

    init {
        subsumsjon.accept(this)
    }

    fun resultat() = mapper.createObjectNode().also {
        it.set("fakta", faktaNode)
        it.set("root", root)
    }

    override fun toString() =
            ObjectMapper().writerWithDefaultPrettyPrinter<ObjectWriter>().writeValueAsString(resultat())

    override fun preVisit(subsumsjon: EnkelSubsumsjon, regel: Regel, fakta: Set<Faktum<*>>) {
        subsumsjonNode(subsumsjon, regel.typeNavn).also { it ->
            it.set("fakta", mapper.valueToTree(fakta.map { it.id }))
        }
    }

    override fun postVisit(subsumsjon: EnkelSubsumsjon, regel: Regel, fakta: Set<Faktum<*>>) {
        root = objectNodes.removeAt(0)
    }

    override fun preVisit(subsumsjon: AlleSubsumsjon) {
        subsumsjonNode(subsumsjon, "alle").also { subsumsjonNode ->
            mapper.createArrayNode().also { arrayNode ->
                subsumsjonNode.set("subsumsjoner", arrayNode)
                arrayNodes.add(0, arrayNode)
            }
        }
    }

    private fun subsumsjonNode(subsumsjon: Subsumsjon, regelType: String) =
            mapper.createObjectNode().also { subsumsjonNode ->
                subsumsjoner[subsumsjon] = subsumsjonNode
                objectNodes.add(0, subsumsjonNode)
                arrayNodes.first().add(subsumsjonNode)
                subsumsjonNode.put("navn", subsumsjon.navn)
                subsumsjonNode.put("kclass", subsumsjon.javaClass.simpleName)
                subsumsjonNode.put("regelType", regelType)
            }

    override fun postVisit(subsumsjon: AlleSubsumsjon) {
        objectNodes.removeAt(0).also {
            it.set("subsumsjoner", arrayNodes.removeAt(0))
            root = it
        }
    }

    override fun preVisit(subsumsjon: MinstEnAvSubsumsjon) {
        subsumsjonNode(subsumsjon, "minstEnAv").also { subsumsjonNode ->
            mapper.createArrayNode().also { arrayNode ->
                subsumsjonNode.set("subsumsjoner", arrayNode)
                arrayNodes.add(0, arrayNode)
            }
        }
    }

    override fun postVisit(subsumsjon: MinstEnAvSubsumsjon) {
        objectNodes.removeAt(0).also {
            arrayNodes.removeAt(0)
            root = it
        }
    }

    override fun <R : Comparable<R>> preVisit(faktum: UtledetFaktum<R>, id: Int, avhengigeFakta: List<Faktum<*>>, children: Set<Faktum<*>>, svar: R) {
        if (id in faktumIder) return
        mapper.createObjectNode().also { faktumNode ->
            faktumNode.put("navn", faktum.navn.toString())
            faktumNode.put("id", id)
            faktumNode.set("avhengigFakta", mapper.valueToTree(avhengigeFakta.map { it.id }))
            faktumNode.set("fakta", mapper.valueToTree(children.map { it.id }))
            faktumNode.putR(svar)
            faktaNode.add(faktumNode)
        }
        faktumIder.add(id)
    }

    override fun <R : Comparable<R>> preVisit(faktum: UtledetFaktum<R>, id: Int, avhengigeFakta: List<Faktum<*>>, children: Set<Faktum<*>>) {
        if (id in faktumIder) return
        mapper.createObjectNode().also { faktumNode ->
            faktumNode.put("navn", faktum.navn.toString())
            faktumNode.put("id", id)
            faktumNode.set("avhengigFakta", mapper.valueToTree(avhengigeFakta.map { it.id }))
            faktumNode.set("fakta", mapper.valueToTree(children.map { it.id }))
            faktaNode.add(faktumNode)
        }
        faktumIder.add(id)
    }

    override fun preVisitGyldig(parent: Subsumsjon, child: Subsumsjon) {
        arrayNodes.add(0, mapper.createArrayNode())
    }

    override fun postVisitGyldig(parent: Subsumsjon, child: Subsumsjon) {
        subsumsjoner[child]?.let {
            objectNodes.first().set("gyldig", it)
        }
        arrayNodes.removeAt(0)
    }

    override fun preVisitUgyldig(parent: Subsumsjon, child: Subsumsjon) {
        arrayNodes.add(0, mapper.createArrayNode())
    }

    override fun postVisitUgyldig(parent: Subsumsjon, child: Subsumsjon) {
        subsumsjoner[child]?.let {
            objectNodes.first().set("ugyldig", it)
        }
        arrayNodes.removeAt(0)
    }

    override fun <R : Comparable<R>> visit(faktum: GrunnleggendeFaktum<R>, tilstand: Faktum.FaktumTilstand, id: Int, avhengigeFakta: List<Faktum<*>>) {
        if (id in faktumIder) return
        mapper.createObjectNode().also { faktumNode ->
            faktumNode.put("navn", faktum.navn.toString())
            faktumNode.put("id", id)
            faktumNode.set("avhengigFakta", mapper.valueToTree(avhengigeFakta.map { it.id }))
            faktaNode.add(faktumNode)
        }
        faktumIder.add(id)
    }

    override fun <R : Comparable<R>> visit(faktum: GrunnleggendeFaktum<R>, tilstand: Faktum.FaktumTilstand, id: Int, avhengigeFakta: List<Faktum<*>>, svar: R) {
        if (id in faktumIder) return
        mapper.createObjectNode().also { faktumNode ->
            faktumNode.put("navn", faktum.navn.toString())
            faktumNode.put("id", id)
            faktumNode.set("avhengigFakta", mapper.valueToTree(avhengigeFakta.map { it.id }))
            faktumNode.putR(svar)
            faktaNode.add(faktumNode)
        }
        faktumIder.add(id)
    }
}

private fun <R : Comparable<R>> ObjectNode.putR(svar: R) {
    when {
        svar is Boolean -> this.put("svar", svar)
        svar is Int -> this.put("svar", svar)
        svar is Double -> this.put("svar", svar)
        svar is String -> this.put("svar", svar)
        svar is LocalDate -> this.put("svar", svar.toString())
        svar is Dokument -> this.put("svar", svar.toUrl())
        else -> throw IllegalArgumentException("Ukjent datatype")
    }
}
