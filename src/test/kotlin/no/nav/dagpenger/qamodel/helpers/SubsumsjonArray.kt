package no.nav.dagpenger.qamodel.helpers

import no.nav.dagpenger.qamodel.subsumsjon.Subsumsjon
import no.nav.dagpenger.qamodel.visitor.SubsumsjonVisitor

internal class SubsumsjonArray(subsumsjon: Subsumsjon): SubsumsjonVisitor {
    init {
        subsumsjon.accept(this)
    }

}