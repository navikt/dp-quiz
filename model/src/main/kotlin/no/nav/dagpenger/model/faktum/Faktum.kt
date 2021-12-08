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

        internal fun List<Faktum<*>>.deepCopy(søknadprosess: Søknadprosess): List<Faktum<*>> = this
            .map { prototype ->
                søknadprosess.faktum(prototype.faktumId).also {
                    prototype.deepCopyAvhengigheter(it, søknadprosess)
                }
            }
            .also {
                require(it.size == this.size) { "Mangler fakta" }
            }

        internal fun List<Faktum<*>>.deepCopy(indeks: Int, søknad: Søknad): List<Faktum<*>> = this
            .map { faktum ->
                faktum.deepCopy(indeks, søknad)
            }

        private val prioritet = listOf(
            GrunnleggendeFaktum::class.java,
            ValgFaktum::class.java,
            TemplateFaktum::class.java,
            GeneratorFaktum::class.java,
            UtledetFaktum::class.java
        )
    }

    fun <R> reflection(block: (Int, Int) -> R) = faktumId.reflection(block)

    abstract fun clazz(): Class<R>

    internal abstract fun bygg(byggetFakta: MutableMap<FaktumId, Faktum<*>>): Faktum<*>

    open fun besvar(r: R, besvarer: String? = null): Faktum<R> = this.also { avhengigeFakta.forEach { it.tilUbesvart() } }

    open fun rehydrer(r: R, besvarer: String?): Faktum<R> = this

    protected open fun tilUbesvart() {
        throw IllegalStateException("Kan ikke sette utleda faktum til ubesvart")
    }

    abstract fun svar(): R

    abstract fun besvartAv(): String?

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

        return prioritet(this) - prioritet(challenger)
    }

    private fun prioritet(faktum: Faktum<*>) =
        if (!prioritet.contains(faktum::class.java)) throw Exception("Mangler prioritet for ${faktum::class.simpleName}")
        else prioritet.indexOf(faktum::class.java)

    internal fun leggTilAvhengigheter(fakta: MutableSet<Faktum<*>>) {
        fakta.addAll(avhengerAvFakta)
    }

    internal fun sjekkAvhengigheter() {
        if (avhengerAvFakta.isEmpty())
            throw IllegalArgumentException("Mangler avhengighet på godkjenningsfaktum: $this")
    }

    fun harRolle(rolle: Rolle) = rolle in roller
}

fun List<Faktum<*>>.erBesvart() = this.all { it.erBesvart() }
