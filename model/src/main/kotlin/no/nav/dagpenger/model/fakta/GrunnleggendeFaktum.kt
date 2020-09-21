package no.nav.dagpenger.model.fakta

import no.nav.dagpenger.model.fakta.Faktum.FaktumTilstand
import no.nav.dagpenger.model.visitor.SubsumsjonVisitor
import no.nav.dagpenger.model.visitor.SøknadVisitor

class GrunnleggendeFaktum<R : Comparable<R>> internal constructor(override val navn: FaktumNavn, private val roller: MutableSet<Rolle>) : Faktum<R> {
    override val avhengigeFakta: MutableList<Faktum<*>> = mutableListOf()
    private var tilstand: Tilstand = Ukjent
    private lateinit var gjeldendeSvar: R

    init { if (roller.isEmpty()) roller.add(Rolle.søker) }

    override fun besvar(r: R, rolle: Rolle) = this.apply {
        if (rolle !in roller) throw IllegalAccessError("Rollen $rolle kan ikke besvare faktum")
        gjeldendeSvar = r
        tilstand = Kjent
        super.besvar(r, rolle)
    }

    override fun svar(): R = tilstand.svar(this)

    override fun erBesvart() = tilstand == Kjent

    override fun tilUbesvart() {
        tilstand = Ukjent
    }

    override fun accept(visitor: SubsumsjonVisitor) {
        tilstand.accept(this, visitor)
    }

    override fun accept(visitor: SøknadVisitor){
        tilstand.accept(this, visitor)
    }

    override fun grunnleggendeFakta() = setOf(this)

    override fun leggTilHvis(kode: FaktumTilstand, fakta: MutableSet<GrunnleggendeFaktum<*>>) {
        if (tilstand.kode == kode) fakta.add(this)
    }

    private interface Tilstand {
        val kode: FaktumTilstand

        fun <R : Comparable<R>> accept(faktum: GrunnleggendeFaktum<R>, visitor: SubsumsjonVisitor)
        fun <R : Comparable<R>> accept(faktum: GrunnleggendeFaktum<R>, visitor: SøknadVisitor)
        fun <R : Comparable<R>> svar(faktum: GrunnleggendeFaktum<R>): R =
            throw IllegalStateException("Faktumet er ikke kjent enda")
    }

    private object Ukjent : Tilstand {
        override val kode = FaktumTilstand.Ukjent
        override fun <R : Comparable<R>> accept(faktum: GrunnleggendeFaktum<R>, visitor: SubsumsjonVisitor) {
            visitor.visit(faktum, kode, faktum.id, faktum.avhengigeFakta, faktum.roller)
        }

        override fun <R : Comparable<R>> accept(faktum: GrunnleggendeFaktum<R>, visitor: SøknadVisitor) {
            visitor.visit(faktum, kode, faktum.id, faktum.avhengigeFakta, faktum.roller)
        }
    }

    private object Kjent : Tilstand {
        override val kode = FaktumTilstand.Kjent
        override fun <R : Comparable<R>> accept(faktum: GrunnleggendeFaktum<R>, visitor: SubsumsjonVisitor) {
            visitor.visit(faktum, Ukjent.kode, faktum.id, faktum.avhengigeFakta, faktum.roller, faktum.gjeldendeSvar)
        }

        override fun <R : Comparable<R>> accept(faktum: GrunnleggendeFaktum<R>, visitor: SøknadVisitor) {
            visitor.visit(faktum, Ukjent.kode, faktum.id, faktum.avhengigeFakta, faktum.roller, faktum.gjeldendeSvar)
        }

        override fun <R : Comparable<R>> svar(faktum: GrunnleggendeFaktum<R>) = faktum.gjeldendeSvar
    }
}
