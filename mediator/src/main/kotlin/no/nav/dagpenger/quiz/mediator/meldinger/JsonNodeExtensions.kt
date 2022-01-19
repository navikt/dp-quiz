package no.nav.dagpenger.quiz.mediator.meldinger

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import no.nav.dagpenger.model.faktum.Envalg
import no.nav.dagpenger.model.faktum.Flervalg
import no.nav.dagpenger.model.faktum.Tekst

fun JsonNode.asEnvalg(): Envalg {
    val svarene = this as ArrayNode
    val valgteSvaralternativer = svarene.map { it.asText() }
    return Envalg(*valgteSvaralternativer.toTypedArray())
}

fun JsonNode.asFlervalg(): Flervalg {
    val svarene = this as ArrayNode
    val valgteSvaralternativer = svarene.map { it.asText() }
    return Flervalg(*valgteSvaralternativer.toTypedArray())
}

fun JsonNode.asTekst() = Tekst(asText())
