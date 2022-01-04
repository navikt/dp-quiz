package no.nav.dagpenger.quiz.mediator.meldinger

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import no.nav.dagpenger.model.faktum.Valg

fun JsonNode.asValg(): Valg {
    val svarene = this as ArrayNode
    if (svarene.isEmpty) {
        throw IllegalArgumentException("Valg må alltid inneholde valgte svaralternativer")
    }

    val valgteSvaralternativer = svarene.map { it.asText() }
    return Valg(*valgteSvaralternativer.toTypedArray())
}
