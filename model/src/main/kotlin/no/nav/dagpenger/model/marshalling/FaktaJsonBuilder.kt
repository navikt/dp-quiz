package no.nav.dagpenger.model.marshalling

import com.fasterxml.jackson.databind.node.ObjectNode
import no.nav.dagpenger.model.faktum.Fakta
import no.nav.dagpenger.model.visitor.FaktaVisitor
import java.util.UUID

internal class FaktaJsonBuilder(fakta: Fakta) : FaktumJsonBuilder(), FaktaVisitor {
    init {
        fakta.accept(this)
    }

    override fun preVisit(fakta: Fakta, fnr: String, versjonId: Int, uuid: UUID) {
        mapper.createObjectNode().also { faktaNode ->
            objectNodes.add(0, faktaNode)
            faktaNode.put("fnr", fnr)
            faktaNode.put("versjonId", versjonId)
            faktaNode.put("uuid", uuid.toString())
        }
    }

    override fun postVisit(fakta: Fakta, fnr: String, versjonId: Int, uuid: UUID) {
        objectNodes.removeAt(0).also { jsonNode ->
            root = jsonNode
            jsonNode.set("faktum", faktaNode)
        }
    }

    override fun resultat(): ObjectNode = mapper.createObjectNode().also {
        it.set("fakta", root)
    }
}
