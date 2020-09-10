package no.nav.dagpenger.qamodel.søknad

import no.nav.dagpenger.qamodel.subsumsjon.Subsumsjon

class Søknad(vararg seksjoner: Seksjon) {
    private val seksjoner = seksjoner.toList()
    infix fun nesteSeksjon(subsumsjon: Subsumsjon) = seksjoner.first{ subsumsjon.nesteFakta() in it }
}
