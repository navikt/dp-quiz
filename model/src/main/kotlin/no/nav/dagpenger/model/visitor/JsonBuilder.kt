package no.nav.dagpenger.model.visitor

import com.fasterxml.jackson.databind.node.ObjectNode
import no.nav.dagpenger.model.fakta.Faktum
import no.nav.dagpenger.model.regel.Regel
import no.nav.dagpenger.model.subsumsjon.AlleSubsumsjon
import no.nav.dagpenger.model.subsumsjon.EnkelSubsumsjon
import no.nav.dagpenger.model.subsumsjon.MinstEnAvSubsumsjon
import no.nav.dagpenger.model.subsumsjon.Subsumsjon

class JsonBuilder(private val subsumsjon: Subsumsjon) : FaktumJsonBuilder(), SubsumsjonVisitor {
    private val subsumsjoner = mutableMapOf<Subsumsjon, ObjectNode>()

    init {
        subsumsjon.accept(this)
    }

    override fun preVisit(subsumsjon: EnkelSubsumsjon, regel: Regel, fakta: Set<Faktum<*>>, resultat: Boolean?) {
        subsumsjonNode(subsumsjon, regel.typeNavn, resultat).also { it ->
            it.set("fakta", mapper.valueToTree(fakta.map { it.id }))
        }
    }

    override fun postVisit(subsumsjon: EnkelSubsumsjon, regel: Regel, fakta: Set<Faktum<*>>, resultat: Boolean?) {
        root = objectNodes.removeAt(0)
    }

    override fun preVisit(subsumsjon: AlleSubsumsjon, resultat: Boolean?) {
        subsumsjonNode(subsumsjon, "alle", resultat)
        arrayNodes.add(0, mapper.createArrayNode())
    }

    override fun postVisit(subsumsjon: AlleSubsumsjon, resultat: Boolean?) {
        objectNodes.removeAt(0).also {
            it.set("subsumsjoner", arrayNodes.removeAt(0))
            root = it
        }
    }

    private fun subsumsjonNode(subsumsjon: Subsumsjon, regelType: String, resultat: Boolean?) =
        mapper.createObjectNode().also { subsumsjonNode ->
            subsumsjoner[subsumsjon] = subsumsjonNode
            objectNodes.add(0, subsumsjonNode)
            arrayNodes.first().add(subsumsjonNode)
            subsumsjonNode.put("navn", subsumsjon.navn)
            subsumsjonNode.put("kclass", subsumsjon.javaClass.simpleName)
            subsumsjonNode.put("regelType", regelType)
            subsumsjonNode.put("resultat", resultat)
        }

    override fun preVisit(subsumsjon: MinstEnAvSubsumsjon, resultat: Boolean?) {
        subsumsjonNode(subsumsjon, "minstEnAv", resultat)
        arrayNodes.add(0, mapper.createArrayNode())
    }

    override fun postVisit(subsumsjon: MinstEnAvSubsumsjon, resultat: Boolean?) {
        objectNodes.removeAt(0).also {
            it.set("subsumsjoner", arrayNodes.removeAt(0))
            root = it
        }
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
}
