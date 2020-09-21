package no.nav.dagpenger.model.visitor

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.ObjectWriter
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import no.nav.dagpenger.model.søknad.Søknad
import java.util.UUID

class SøknadJsonBuilder(private val søknad: Søknad) : SøknadVisitor {
    private val mapper = ObjectMapper()
    private var rootSøknad: ObjectNode = mapper.createObjectNode()
    private val arrayNodes: MutableList<ArrayNode> = mutableListOf(mapper.createArrayNode())
    private val objectNodes: MutableList<ObjectNode> = mutableListOf()

    init {
        søknad.accept(this)
    }

    fun resultat() = rootSøknad

    override fun toString() =
        ObjectMapper().writerWithDefaultPrettyPrinter<ObjectWriter>().writeValueAsString(resultat())

    override fun preVisit(søknad: Søknad, uuid: UUID) {
        mapper.createObjectNode().also { søknadNode ->
            objectNodes.add(0, søknadNode)
            søknadNode.put("uuid", uuid.toString())
        }
    }

    override fun postVisit(søknad: Søknad) {
        rootSøknad = objectNodes.removeAt(0)
    }
}
