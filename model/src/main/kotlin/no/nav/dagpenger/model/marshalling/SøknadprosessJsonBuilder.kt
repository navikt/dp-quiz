package no.nav.dagpenger.model.marshalling

import no.nav.dagpenger.model.faktum.Faktum
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.seksjon.Seksjon
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.model.visitor.SøknadprosessVisitor
import java.util.UUID

class SøknadprosessJsonBuilder(private val søknadprosess: Søknadprosess) : FaktumJsonBuilder(), SøknadprosessVisitor {

    init {
        søknadprosess.accept(this)
    }

    override fun preVisit(søknadprosess: Søknadprosess, uuid: UUID) {
        mapper.createObjectNode().also { søknadNode ->
            objectNodes.add(0, søknadNode)
            søknadNode.put("uuid", uuid.toString())
        }
    }

    override fun postVisit(søknadprosess: Søknadprosess) {
        objectNodes.removeAt(0).also { søknadNode ->
            root = søknadNode
            søknadNode.set("seksjoner", arrayNodes.removeAt(0))
        }
    }

    override fun preVisit(seksjon: Seksjon, rolle: Rolle, fakta: Set<Faktum<*>>, indeks: Int) {
        mapper.createObjectNode().also { seksjonNode ->
            arrayNodes.first().add(seksjonNode)
            seksjonNode.put("navn", seksjon.navn)
            seksjonNode.put("rolle", rolle.typeNavn)
            seksjonNode.set("fakta", mapper.valueToTree(fakta.map { it.id }))
        }
    }
}
