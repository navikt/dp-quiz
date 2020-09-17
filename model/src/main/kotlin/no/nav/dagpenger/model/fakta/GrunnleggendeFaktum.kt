package no.nav.dagpenger.model.fakta

import no.nav.dagpenger.model.fakta.Faktum.FaktumTilstand
import no.nav.dagpenger.model.visitor.SubsumsjonVisitor

class GrunnleggendeFaktum<R : Comparable<R>> internal constructor(override val navn: FaktumNavn, private val roller: MutableSet<Rolle>) : Faktum<R> {
    private var tilstand: Tilstand = Ukjent
    private lateinit var gjeldendeSvar: R

    init { if (roller.isEmpty()) roller.add(Rolle.søker) }

    override fun besvar(r: R, rolle: Rolle) = this.apply {
        if (rolle !in roller) throw IllegalAccessError("Rollen $rolle kan ikke besvare faktum")
        gjeldendeSvar = r
        tilstand = Kjent
    }

    override fun svar(): R = tilstand.svar(this)

    override fun erBesvart() = tilstand == Kjent

    override fun accept(visitor: SubsumsjonVisitor) {
        tilstand.accept(this, visitor)
    }

    override fun grunnleggendeFakta() = setOf(this)

    override fun leggTilHvis(kode: FaktumTilstand, fakta: MutableSet<GrunnleggendeFaktum<*>>) {
        if (tilstand.kode == kode) fakta.add(this)
    }

    private interface Tilstand {
        val kode: FaktumTilstand

        fun <R : Comparable<R>> accept(faktum: GrunnleggendeFaktum<R>, visitor: SubsumsjonVisitor)
        fun <R : Comparable<R>> svar(faktum: GrunnleggendeFaktum<R>): R =
            throw IllegalStateException("Faktumet er ikke kjent enda")
    }

    private object Ukjent : Tilstand {
        override val kode = FaktumTilstand.Ukjent
        override fun <R : Comparable<R>> accept(faktum: GrunnleggendeFaktum<R>, visitor: SubsumsjonVisitor) {
            visitor.visit(faktum, kode)
        }
    }

    private object Kjent : Tilstand {
        override val kode = FaktumTilstand.Kjent
        override fun <R : Comparable<R>> accept(faktum: GrunnleggendeFaktum<R>, visitor: SubsumsjonVisitor) {
            visitor.visit(faktum, Ukjent.kode, faktum.gjeldendeSvar)
        }

        override fun <R : Comparable<R>> svar(faktum: GrunnleggendeFaktum<R>) = faktum.gjeldendeSvar
    }
}
