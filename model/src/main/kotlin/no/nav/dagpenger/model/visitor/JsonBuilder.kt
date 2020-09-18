package no.nav.dagpenger.model.visitor

import com.fasterxml.jackson.databind.ObjectMapper
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
    private val fakta = mutableListOf<Faktum<*>>()
    private val arrayNodes: MutableList<ArrayNode> = mutableListOf(mapper.createArrayNode())
    private lateinit var behaviorNodes: ArrayNode
    private val objectNodes: MutableList<ObjectNode> = mutableListOf()



    init {
        subsumsjon.accept(this)
    }

    override fun preVisit(subsumsjon: EnkelSubsumsjon, regel: Regel) {
        mapper.createObjectNode().also { subsumsjonNode ->
            objectNodes.add(0, subsumsjonNode)
            subsumsjonNode.put("navn", subsumsjon.navn)
            behaviorNodes = mapper.createArrayNode()

        }
    }

    override fun postVisit(subsumsjon: EnkelSubsumsjon, regel: Regel) {
        objectNodes.removeAt(0).also {
            it.set("fakta", arrayNodes.removeAt(0))
            root = it
        }
    }


    override fun preVisit(subsumsjon: AlleSubsumsjon) {
        super.preVisit(subsumsjon)
    }

    override fun preVisit(subsumsjon: MinstEnAvSubsumsjon) {
        super.preVisit(subsumsjon)
    }

    override fun <R : Comparable<R>> preVisit(faktum: UtledetFaktum<R>, id: Int, svar: R) {
        super.preVisit(faktum, id, svar)
    }

    override fun <R : Comparable<R>> preVisit(faktum: UtledetFaktum<R>, id: Int) {
        super.preVisit(faktum, id)
    }

    override fun <R : Comparable<R>> preVisit(parent: UtledetFaktum<R>, id: Int, children: Set<Faktum<*>>) {
        fakta.add(0, parent)
    }

    override fun <R : Comparable<R>> postVisit(parent: UtledetFaktum<R>, id: Int, children: Set<Faktum<*>>) {
        fakta.removeAt(0)
    }

    override fun postVisit(subsumsjon: AlleSubsumsjon) {
        super.postVisit(subsumsjon)
    }

    override fun postVisit(subsumsjon: MinstEnAvSubsumsjon) {
        super.postVisit(subsumsjon)
    }

    override fun <R : Comparable<R>> postVisit(faktum: UtledetFaktum<R>, id: Int) {
        super.postVisit(faktum, id)
    }

    override fun preVisitGyldig(parent: Subsumsjon, child: Subsumsjon) {
        super.preVisitGyldig(parent, child)
    }

    override fun postVisitGyldig(parent: Subsumsjon, child: Subsumsjon) {
        super.postVisitGyldig(parent, child)
    }

    override fun preVisitUgyldig(parent: Subsumsjon, child: Subsumsjon) {
        super.preVisitUgyldig(parent, child)
    }

    override fun postVisitUgyldig(parent: Subsumsjon, child: Subsumsjon) {
        super.postVisitUgyldig(parent, child)
    }

    override fun <R : Comparable<R>> visit(faktum: GrunnleggendeFaktum<R>, tilstand: Faktum.FaktumTilstand, id: Int) {
        mapper.createObjectNode().also { faktumNode ->
            faktumNode.put("navn", faktum.navn.toString())
            faktumNode.put("id", id)
            arrayNodes.first().add(faktumNode)
        }
    }

    override fun <R : Comparable<R>> visit(faktum: GrunnleggendeFaktum<R>, tilstand: Faktum.FaktumTilstand, id: Int, svar: R) {
        mapper.createObjectNode().also { faktumNode ->
            faktumNode.put("navn", faktum.navn.toString())
            faktumNode.put("id", id)
            faktumNode.putR(svar)
        }
    }

    fun resultat() = root
}

private fun <R: Comparable<R>> ObjectNode.putR(svar: R) {
    when{
        svar is Boolean -> this.put("svar", svar)
        svar is Int -> this.put("svar", svar)
        svar is Double -> this.put("svar", svar)
        svar is String -> this.put("svar", svar)
        svar is LocalDate -> this.put("svar", svar.toString())
        svar is Dokument -> this.put("svar", svar.toUrl())
        else -> throw IllegalArgumentException("Ukjent datatype")
    }
}

