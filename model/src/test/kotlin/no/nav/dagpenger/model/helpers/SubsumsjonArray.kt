package no.nav.dagpenger.model.helpers

import no.nav.dagpenger.model.subsumsjon.Subsumsjon
import no.nav.dagpenger.model.visitor.SubsumsjonVisitor

internal class SubsumsjonArray(subsumsjon: Subsumsjon) : SubsumsjonVisitor {
    init {
        subsumsjon.accept(this)
    }
}
