package no.nav.dagpenger.model.fakta

import java.time.LocalDate

class Dokument(private val opplastingsdato: LocalDate) : Comparable<Dokument> {
    override fun compareTo(other: Dokument): Int {
        return this.opplastingsdato.compareTo(other.opplastingsdato)
    }
}
