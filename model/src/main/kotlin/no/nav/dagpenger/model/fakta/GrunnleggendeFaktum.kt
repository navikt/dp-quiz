package no.nav.dagpenger.model.fakta

import no.nav.dagpenger.model.fakta.Faktum.FaktumTilstand
import no.nav.dagpenger.model.visitor.FaktumVisitor

class GrunnleggendeFaktum<R : Comparable<R>> internal constructor(override val navn: FaktumNavn, private val clazz: Class<R>) : Faktum<R> {
    override val avhengigeFakta: MutableList<Faktum<*>> = mutableListOf()
    private var tilstand: Tilstand = Ukjent
    private lateinit var gjeldendeSvar: R
    private val roller = mutableSetOf<Rolle>()

    override fun clazz() = clazz

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

    override fun accept(visitor: FaktumVisitor) {
        tilstand.accept(this, visitor)
    }

    override fun grunnleggendeFakta() = setOf(this)

    override fun leggTilHvis(kode: FaktumTilstand, fakta: MutableSet<GrunnleggendeFaktum<*>>) {
        if (tilstand.kode == kode) fakta.add(this)
    }

    override fun add(rolle: Rolle) = roller.add(rolle)

    override fun toString() = navn.toString()

    private interface Tilstand {
        val kode: FaktumTilstand

        fun <R : Comparable<R>> accept(faktum: GrunnleggendeFaktum<R>, visitor: FaktumVisitor)
        fun <R : Comparable<R>> svar(faktum: GrunnleggendeFaktum<R>): R =
            throw IllegalStateException("Faktumet er ikke kjent enda")
    }

    private object Ukjent : Tilstand {
        override val kode = FaktumTilstand.Ukjent
        override fun <R : Comparable<R>> accept(faktum: GrunnleggendeFaktum<R>, visitor: FaktumVisitor) {
            visitor.visit(faktum, kode, faktum.id, faktum.avhengigeFakta, faktum.roller, faktum.clazz)
        }
    }

    private object Kjent : Tilstand {
        override val kode = FaktumTilstand.Kjent
        override fun <R : Comparable<R>> accept(faktum: GrunnleggendeFaktum<R>, visitor: FaktumVisitor) {
            visitor.visit(faktum, Ukjent.kode, faktum.id, faktum.avhengigeFakta, faktum.roller, faktum.clazz, faktum.gjeldendeSvar)
        }

        override fun <R : Comparable<R>> svar(faktum: GrunnleggendeFaktum<R>) = faktum.gjeldendeSvar
    }
}
