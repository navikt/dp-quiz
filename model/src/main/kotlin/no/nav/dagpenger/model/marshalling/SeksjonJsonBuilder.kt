package no.nav.dagpenger.model.marshalling

import no.nav.dagpenger.model.faktagrupper.Seksjon
import no.nav.dagpenger.model.faktum.Faktum
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.visitor.FaktagrupperVisitor

class SeksjonJsonBuilder(private val seksjon: Seksjon) : FaktumJsonBuilder(), FaktagrupperVisitor {

    init {
        seksjon.accept(this)
    }

    override fun preVisit(seksjon: Seksjon, rolle: Rolle, fakta: Set<Faktum<*>>) {
        mapper.createObjectNode().also { seksjonNode ->
            root = seksjonNode
            seksjonNode.put("rolle", rolle.name)
            seksjonNode.put("navn", seksjon.navn)
            seksjonNode.set("fakta", mapper.valueToTree(fakta.map { it.id }))
        }
    }
}
