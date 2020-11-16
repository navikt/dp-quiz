package no.nav.dagpenger.model.faktum

import no.nav.dagpenger.model.seksjon.Seksjon
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.model.visitor.FaktumVisitor

abstract class Faktum<R : Comparable<R>> internal constructor(
    internal val faktumId: FaktumId,
    val navn: String,
    protected val avhengigeFakta: MutableSet<Faktum<*>> = mutableSetOf(),
    protected val avhengerAvFakta: MutableSet<Faktum<*>> = mutableSetOf(),
    protected val roller: MutableSet<Rolle> = mutableSetOf()
) : Comparable<Faktum<*>> {
    val id: String get() = faktumId.id

    companion object {
        private fun Faktum<*>.deepCopyAvhengigheter(faktum: Faktum<*>, søknadprosess: Søknadprosess) {
            faktum.avhengigeFakta.addAll(this.avhengigeFakta.map { søknadprosess.faktum(it.faktumId) })
            faktum.avhengerAvFakta.addAll(this.avhengerAvFakta.map { søknadprosess.faktum(it.faktumId) })
        }

        internal fun Set<Faktum<*>>.deepCopy(søknadprosess: Søknadprosess): Set<Faktum<*>> = this
            .mapNotNull { prototype ->
                søknadprosess.faktum(prototype.faktumId).also {
                    prototype.deepCopyAvhengigheter(it, søknadprosess)
                }
            }
            .toSet()
            .also {
                require(it.size == this.size) { "Mangler fakta" }
            }

        internal fun Set<Faktum<*>>.deepCopy(indeks: Int, søknad: Søknad): Set<Faktum<*>> = this
            .map { faktum ->
                faktum.deepCopy(indeks, søknad)
            }
            .toSet()
    }

    fun <R> reflection(block: (Int, Int) -> R) = faktumId.reflection(block)

    abstract fun clazz(): Class<R>

    internal abstract fun bygg(byggetFakta: MutableMap<FaktumId, Faktum<*>>): Faktum<*>

    open infix fun besvar(r: R): Faktum<R> = this.also { avhengigeFakta.forEach { it.tilUbesvart() } }

    protected open fun tilUbesvart() {
        throw IllegalStateException("Kan ikke sette utleda faktum til ubesvart")
    }

    abstract fun svar(): R

    abstract fun grunnleggendeFakta(): Set<GrunnleggendeFaktum<*>>

    abstract fun leggTilHvis(kode: FaktumTilstand, fakta: MutableSet<GrunnleggendeFaktum<*>>)

    abstract fun erBesvart(): Boolean

    abstract fun accept(visitor: FaktumVisitor)

    internal open fun add(rolle: Rolle): Boolean = roller.add(rolle)

    internal open fun add(seksjon: Seksjon): Boolean = false

    internal open infix fun harAvhengighet(other: Faktum<*>) {
        this.avhengigeFakta.add(other)
        other.avhengerAvFakta.add(this)
    }

    internal open fun deepCopy(indeks: Int, søknad: Søknad): Faktum<*> = this

    enum class FaktumTilstand {
        Ukjent,
        Kjent
    }

    override fun toString() = "$navn med id $id"

    internal open fun tilTemplate(): TemplateFaktum<R> {
        throw IllegalArgumentException("Kan ikke lage template av faktum: $navn $id")
    }

    override fun compareTo(challenger: Faktum<*>): Int {
        if (this::class.java == challenger::class.java) return this.faktumId.compareTo(challenger.faktumId)

        if (this::class.java == GrunnleggendeFaktum::class.java) return -1
        if (challenger::class.java == GrunnleggendeFaktum::class.java) return 1
        if (this::class.java == TemplateFaktum::class.java) return -1
        if (challenger::class.java == TemplateFaktum::class.java) return 1
        if (this::class.java == GeneratorFaktum::class.java) return -1
        if (challenger::class.java == GeneratorFaktum::class.java) return 1
        if (this::class.java == ValgFaktum::class.java) return -1
        if (challenger::class.java == ValgFaktum::class.java) return 1

        throw ClassCastException("Vet ikke hvilken av ${this::class.java} og ${challenger::class.java} som skal sorteres først")
    }

    internal fun leggTilAvhengigheter(fakta: MutableSet<Faktum<*>>) {
        fakta.addAll(avhengerAvFakta)
    }

    internal fun sjekkAvhengigheter() {
        if (avhengerAvFakta.isEmpty())
            throw IllegalArgumentException("Mangler avhengighet på godkjenningsfaktum: $this")
    }

    fun harRolle(rolle: Rolle) = rolle in roller
}

fun Set<Faktum<*>>.erBesvart() = this.all { it.erBesvart() }
