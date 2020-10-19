package no.nav.dagpenger.model.factory

import no.nav.dagpenger.model.fakta.Faktum

interface FaktumFactory <T : Comparable<T>> {
    val faktum: Faktum<T>
}
