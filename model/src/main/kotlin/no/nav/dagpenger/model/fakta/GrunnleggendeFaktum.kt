package no.nav.dagpenger.model.fakta

import no.nav.dagpenger.model.fakta.Faktum.FaktumTilstand
import no.nav.dagpenger.model.visitor.SubsumsjonVisitor

class GrunnleggendeFaktum<R : Comparable<R>>(override val navn: String) : Faktum<R> {
    private var tilstand: Tilstand = Ukjent
    private lateinit var gjeldendeSvar: R

    override infix fun besvar(r: R) = this.apply {
        gjeldendeSvar = r
        tilstand = Kjent
    }

    override fun svar(): R = tilstand.svar(this)

    override fun erBesvart() = tilstand == Kjent

    override fun accept(visitor: SubsumsjonVisitor) {
        tilstand.accept(this, visitor)
    }

    override fun leggTilHvis(kode: FaktumTilstand, fakta: MutableSet<GrunnleggendeFaktum<*>>) {
        if (tilstand.kode == kode) fakta.add(this)
    }

    private interface Tilstand {
        val kode: FaktumTilstand

        fun <R : Comparable<R>> accept(faktum: GrunnleggendeFaktum<R>, visitor: SubsumsjonVisitor)
        fun <R : Comparable<R>> svar(faktum: GrunnleggendeFaktum<R>): R = throw IllegalStateException("Faktumet er ikke kjent enda")
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

