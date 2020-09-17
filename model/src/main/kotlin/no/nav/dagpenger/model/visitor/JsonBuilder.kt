package no.nav.dagpenger.model.visitor

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import no.nav.dagpenger.model.fakta.Faktum
import no.nav.dagpenger.model.subsumsjon.Subsumsjon

class JsonBuilder(private val subsumsjon: Subsumsjon): SubsumsjonVisitor {
    private val mapper = ObjectMapper()
    private var root: ObjectNode = mapper.createObjectNode()


    init {
        subsumsjon.accept(this)
    }

    fun resultat() = root

}