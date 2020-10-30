package no.nav.dagpenger.model.marshalling

import no.nav.dagpenger.model.fakta.Faktum
import no.nav.dagpenger.model.fakta.Rolle
import no.nav.dagpenger.model.faktagrupper.Faktagrupper
import no.nav.dagpenger.model.faktagrupper.Seksjon
import no.nav.dagpenger.model.visitor.SøknadVisitor
import java.util.UUID

class FaktaGrupperJsonBuilder(private val faktagrupper: Faktagrupper) : FaktumJsonBuilder(), SøknadVisitor {

    init {
        faktagrupper.accept(this)
    }

    override fun preVisit(faktagrupper: Faktagrupper, uuid: UUID) {
        mapper.createObjectNode().also { søknadNode ->
            objectNodes.add(0, søknadNode)
            søknadNode.put("uuid", uuid.toString())
        }
    }

    override fun postVisit(faktagrupper: Faktagrupper) {
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
