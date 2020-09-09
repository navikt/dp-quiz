package no.nav.dagpenger.qamodel.regel

import no.nav.dagpenger.qamodel.fakta.Faktum

interface Regel {
    fun konkluder(fakta: List<Faktum<*>>): Boolean
}
