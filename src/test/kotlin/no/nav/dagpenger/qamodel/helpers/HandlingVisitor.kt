package no.nav.dagpenger.qamodel.helpers

import no.nav.dagpenger.qamodel.fakta.Faktum
import no.nav.dagpenger.qamodel.handling.Handling
import no.nav.dagpenger.qamodel.visitor.FaktumVisitor
import java.time.LocalDate

internal class HandlingVisitor(faktum: Faktum<*>) : FaktumVisitor {
    lateinit var jaHandling: Handling<Boolean>
    lateinit var neiHandling: Handling<Boolean>
    lateinit var datoHandling: Handling<LocalDate>

    init {
        faktum.accept(this)
    }

    override fun preVisitJa(handling: Handling<Boolean>) {
        if (!::jaHandling.isInitialized) jaHandling = handling
    }

    override fun preVisitNei(handling: Handling<Boolean>) {
        if (!::neiHandling.isInitialized) neiHandling = handling
    }

    override fun preVisitDato(handling: Handling<LocalDate>) {
        if (!::datoHandling.isInitialized) datoHandling = handling
    }
}

internal val Faktum<Boolean>.ja get() = HandlingVisitor(this).jaHandling
internal val Faktum<Boolean>.nei get() = HandlingVisitor(this).neiHandling
internal val Faktum<LocalDate>.dato get() = HandlingVisitor(this).datoHandling
