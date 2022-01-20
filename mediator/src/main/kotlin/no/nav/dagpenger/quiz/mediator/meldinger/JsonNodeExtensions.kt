package no.nav.dagpenger.quiz.mediator.meldinger

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import no.nav.dagpenger.model.faktum.Envalg
import no.nav.dagpenger.model.faktum.Flervalg
import no.nav.dagpenger.model.faktum.Periode
import no.nav.dagpenger.model.faktum.Tekst
import no.nav.helse.rapids_rivers.asLocalDate
import no.nav.helse.rapids_rivers.asOptionalLocalDate

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

fun JsonNode.asPeriode(): Periode {
    val fom = this["fom"].asLocalDate()
    val tom = this["tom"].asOptionalLocalDate()
    return Periode(fom, tom)
}
