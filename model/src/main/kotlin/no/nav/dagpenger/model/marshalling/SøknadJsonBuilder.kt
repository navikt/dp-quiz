package no.nav.dagpenger.model.marshalling

import no.nav.dagpenger.model.fakta.Faktum
import no.nav.dagpenger.model.fakta.Rolle
import no.nav.dagpenger.model.søknad.Seksjon
import no.nav.dagpenger.model.søknad.Søknad
import no.nav.dagpenger.model.visitor.SøknadVisitor
import java.util.UUID

class SøknadJsonBuilder(private val søknad: Søknad) : FaktumJsonBuilder(), SøknadVisitor {

    init {
        søknad.accept(this)
    }

    override fun preVisit(søknad: Søknad, uuid: UUID) {
        mapper.createObjectNode().also { søknadNode ->
            objectNodes.add(0, søknadNode)
            søknadNode.put("uuid", uuid.toString())
        }
    }

    override fun postVisit(søknad: Søknad) {
        objectNodes.removeAt(0).also { søknadNode ->
            root = søknadNode
            søknadNode.set("seksjoner", arrayNodes.removeAt(0))
        }
    }

    override fun preVisit(seksjon: Seksjon, rolle: Rolle, fakta: Set<Faktum<*>>) {
        mapper.createObjectNode().also { seksjonNode ->
            arrayNodes.first().add(seksjonNode)
            seksjonNode.put("navn", seksjon.navn)
            seksjonNode.put("rolle", rolle.name)
            seksjonNode.set("fakta", mapper.valueToTree(fakta.map { it.id }))
        }
    }
}
