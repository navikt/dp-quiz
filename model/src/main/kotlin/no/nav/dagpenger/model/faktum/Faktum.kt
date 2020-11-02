package no.nav.dagpenger.model.faktum

import no.nav.dagpenger.model.faktagrupper.Faktagrupper
import no.nav.dagpenger.model.faktagrupper.Seksjon
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
        private fun Faktum<*>.deepCopyAvhengigheter(faktum: Faktum<*>, faktagrupper: Faktagrupper) {
            faktum.avhengigeFakta.addAll(this.avhengigeFakta.map { faktagrupper.faktum(it.faktumId) })
            faktum.avhengerAvFakta.addAll(this.avhengerAvFakta.map { faktagrupper.faktum(it.faktumId) })
        }

        internal fun Set<Faktum<*>>.deepCopy(faktagrupper: Faktagrupper): Set<Faktum<*>> = this
            .mapNotNull { prototype ->
                faktagrupper.faktum(prototype.faktumId).also {
                    prototype.deepCopyAvhengigheter(it, faktagrupper)
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

    open fun besvar(r: R, rolle: Rolle = Rolle.søker): Faktum<R> = this.also {
        if (rolle !in roller) throw IllegalAccessError("Rollen $rolle kan ikke besvare faktum")
        avhengigeFakta.forEach { it.tilUbesvart() }
    }

    protected open fun tilUbesvart() {
        throw IllegalStateException("Kan ikke sette utleda faktum til ubesvart")
    }

    abstract fun svar(): R

    abstract fun grunnleggendeFakta(): Set<GrunnleggendeFaktum<*>>

    abstract fun leggTilHvis(kode: FaktumTilstand, fakta: MutableSet<GrunnleggendeFaktum<*>>)

    abstract fun erBesvart(): Boolean

    internal abstract fun accept(visitor: FaktumVisitor)

    internal open fun add(rolle: Rolle): Boolean = roller.add(rolle)

    internal open fun add(seksjon: Seksjon): Boolean = false

    infix fun avhengerAv(other: Faktum<*>) {
        other.avhengigeFakta.add(this)
        this.avhengerAvFakta.add(other)
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

        if (this is GrunnleggendeFaktum) return -1
        if (challenger is GrunnleggendeFaktum) return 1
        if (this is TemplateFaktum) return -1
        if (challenger is TemplateFaktum) return 1
        if (this is GeneratorFaktum) return -1
        if (challenger is GeneratorFaktum) return 1
        if (this is ValgFaktum) return -1
        if (challenger is ValgFaktum) return 1

        throw ClassCastException("Vet ikke hvilken av ${this::class.java} og ${challenger::class.java} som skal sorteres først")
    }
}

fun Set<Faktum<*>>.erBesvart() = this.all { it.erBesvart() }
