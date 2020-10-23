package no.nav.dagpenger.model.fakta

import no.nav.dagpenger.model.søknad.Seksjon
import no.nav.dagpenger.model.søknad.Søknad
import no.nav.dagpenger.model.visitor.FaktumVisitor

abstract class Faktum<R : Comparable<R>> internal constructor(
    internal val faktumId: FaktumId,
    val navn: String,
    protected val avhengigeFakta: MutableSet<Faktum<*>> = mutableSetOf(),
    protected val roller: MutableSet<Rolle> = mutableSetOf()
) : Comparable<Faktum<*>> {

    val id: String get() = faktumId.id

    companion object {
        private fun Faktum<*>.deepCopyAvhengigheter(faktum: Faktum<*>, søknad: Søknad) {
            faktum.avhengigeFakta.addAll(this.avhengigeFakta.map { søknad.faktum(it.faktumId) })
        }

        internal fun Set<Faktum<*>>.deepCopy(søknad: Søknad): Set<Faktum<*>> = this
            .mapNotNull { prototype ->
                søknad.faktum(prototype.faktumId)?.also {
                    prototype.deepCopyAvhengigheter(it, søknad)
                }
            }
            .toSet()
            .also {
                require(it.size == this.size) { "Mangler fakta" }
            }

        internal fun Set<Faktum<*>>.deepCopy(indeks: Int, fakta: Fakta): Set<Faktum<*>> = this
            .map { faktum ->
                faktum.deepCopy(indeks, fakta)
            }
            .toSet()
    }

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
    }

    internal open fun deepCopy(indeks: Int, fakta: Fakta): Faktum<*> = this

    enum class FaktumTilstand {
        Ukjent,
        Kjent
    }

    override fun toString() = "$navn med id $id"

    internal open fun tilTemplate(): TemplateFaktum<R> {
        throw IllegalArgumentException("Kan ikke lage template av faktum: $navn $id")
    }

    override fun compareTo(challenger: Faktum<*>) =
        when (this::class.java to challenger::class.java) {
            GrunnleggendeFaktum::class.java to TemplateFaktum::class.java -> -1
            TemplateFaktum::class.java to GrunnleggendeFaktum::class.java -> 1
            GeneratorFaktum::class.java to TemplateFaktum::class.java -> 1
            TemplateFaktum::class.java to GeneratorFaktum::class.java -> -1
            GeneratorFaktum::class.java to UtledetFaktum::class.java -> -1
            UtledetFaktum::class.java to GeneratorFaktum::class.java -> 1
            else -> {
                this.faktumId.compareTo(challenger.faktumId)
            }
        }
}

fun Set<Faktum<*>>.erBesvart() = this.all { it.erBesvart() }
typealias FaktaRegel <R> = (UtledetFaktum<R>) -> R
