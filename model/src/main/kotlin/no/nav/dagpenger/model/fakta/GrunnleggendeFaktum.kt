package no.nav.dagpenger.model.fakta

import no.nav.dagpenger.model.fakta.Faktum.FaktumTilstand
import no.nav.dagpenger.model.visitor.FaktumVisitor

open class GrunnleggendeFaktum<R : Comparable<R>> internal constructor(
    override val faktumNavn: FaktumNavn,
    private val clazz: Class<R>,
    avhengigeFakta: MutableSet<Faktum<*>>,
    roller: MutableSet<Rolle>
) : Faktum<R>(avhengigeFakta, roller) {
    private var tilstand: Tilstand = Ukjent
    protected lateinit var gjeldendeSvar: R

    internal constructor(navn: FaktumNavn, clazz: Class<R>) : this(navn, clazz, mutableSetOf(), mutableSetOf())

    override fun clazz() = clazz

    override fun besvar(r: R, rolle: Rolle) = this.apply {
        super.besvar(r, rolle)
        gjeldendeSvar = r
        tilstand = Kjent
    }

    override fun svar(): R = tilstand.svar(this)

    override fun erBesvart() = tilstand == Kjent

    override fun tilUbesvart() {
        tilstand = Ukjent
    }

    override fun accept(visitor: FaktumVisitor) {
        faktumNavn.accept(visitor)
        tilstand.accept(this, visitor)
    }

    override fun grunnleggendeFakta() = setOf(this)

    override fun leggTilHvis(kode: FaktumTilstand, fakta: MutableSet<GrunnleggendeFaktum<*>>) {
        if (tilstand.kode == kode) fakta.add(this)
    }

    protected open fun acceptUtenSvar(visitor: FaktumVisitor) {
        visitor.visit(this, Ukjent.kode, id, avhengigeFakta, roller, clazz)
    }

    protected open fun acceptMedSvar(visitor: FaktumVisitor) {
        visitor.visit(this, Kjent.kode, id, avhengigeFakta, roller, clazz, gjeldendeSvar)
    }

    override fun toString() = faktumNavn.toString()

    private interface Tilstand {
        val kode: FaktumTilstand

        fun <R : Comparable<R>> accept(faktum: GrunnleggendeFaktum<R>, visitor: FaktumVisitor)
        fun <R : Comparable<R>> svar(faktum: GrunnleggendeFaktum<R>): R =
            throw IllegalStateException("Faktumet er ikke kjent enda")
    }

    private object Ukjent : Tilstand {
        override val kode = FaktumTilstand.Ukjent
        override fun <R : Comparable<R>> accept(faktum: GrunnleggendeFaktum<R>, visitor: FaktumVisitor) {
            faktum.acceptUtenSvar(visitor)
        }
    }

    private object Kjent : Tilstand {
        override val kode = FaktumTilstand.Kjent
        override fun <R : Comparable<R>> accept(faktum: GrunnleggendeFaktum<R>, visitor: FaktumVisitor) {
            faktum.acceptMedSvar(visitor)
        }

        override fun <R : Comparable<R>> svar(faktum: GrunnleggendeFaktum<R>) = faktum.gjeldendeSvar
    }
}
