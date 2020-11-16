package no.nav.dagpenger.model.marshalling

import com.fasterxml.jackson.databind.node.ArrayNode
import no.nav.dagpenger.model.faktum.Faktum
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.seksjon.Seksjon
import no.nav.dagpenger.model.visitor.SøknadprosessVisitor

class SeksjonJsonBuilder(private val seksjon: Seksjon) : FaktumJsonBuilder(), SøknadprosessVisitor {

    init {
        seksjon.accept(this)
    }

    override fun preVisit(seksjon: Seksjon, rolle: Rolle, fakta: Set<Faktum<*>>, indeks: Int) {
        mapper.createObjectNode().also { seksjonNode ->
            root = seksjonNode
            seksjonNode.put("rolle", rolle.name)
            seksjonNode.put("navn", seksjon.navn)
            seksjonNode.set("fakta", mapper.valueToTree(fakta.map { it.id }))
        }
    }

    override fun preVisitAvhengerAv(seksjon: Seksjon, avhengerAvFakta: Set<Faktum<*>>) {
        avhengerAvFakta.forEach { (root["fakta"] as ArrayNode).add(it.id) }
    }
}
