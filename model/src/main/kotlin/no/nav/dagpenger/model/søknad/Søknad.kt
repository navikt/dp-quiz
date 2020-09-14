package no.nav.dagpenger.model.søknad

import no.nav.dagpenger.model.subsumsjon.Subsumsjon

class Søknad(vararg seksjoner: Seksjon) {
    private val seksjoner = seksjoner.toList()
    infix fun nesteSeksjon(subsumsjon: Subsumsjon) = seksjoner.first { subsumsjon.nesteFakta() in it }
}
