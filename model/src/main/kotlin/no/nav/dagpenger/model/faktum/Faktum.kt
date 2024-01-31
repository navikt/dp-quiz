package no.nav.dagpenger.model.faktum

import no.nav.dagpenger.model.seksjon.Prosess
import no.nav.dagpenger.model.seksjon.Seksjon
import no.nav.dagpenger.model.visitor.FaktumVisitor

abstract class Faktum<R : Comparable<R>> internal constructor(
    internal val faktumId: FaktumId,
    val navn: String,
    protected val avhengigeFakta: MutableSet<Faktum<*>> = mutableSetOf(),
    protected val avhengerAvFakta: MutableSet<Faktum<*>> = mutableSetOf(),
    protected val roller: MutableSet<Rolle> = mutableSetOf(),
) : Comparable<Faktum<*>> {
    val id: String get() = faktumId.id

    companion object {
        internal fun Collection<Faktum<*>>.erAlleBesvart() = this.all { it.erBesvart() }
        private fun Faktum<*>.deepCopyAvhengigheter(faktum: Faktum<*>, prosess: Prosess) {
            faktum.avhengigeFakta.addAll(this.avhengigeFakta.map { prosess.faktum(it.faktumId) })
            faktum.avhengerAvFakta.addAll(this.avhengerAvFakta.map { prosess.faktum(it.faktumId) })
        }

        internal fun List<Faktum<*>>.deepCopy(prosess: Prosess): List<Faktum<*>> = this
            .map { prototype ->
                prosess.faktum(prototype.faktumId).also {
                    prototype.deepCopyAvhengigheter(it, prosess)
                }
            }
            .also {
                require(it.size == this.size) { "Mangler fakta" }
            }

        internal fun List<Faktum<*>>.deepCopy(indeks: Int, fakta: Fakta): List<Faktum<*>> = this
            .map { faktum ->
                faktum.deepCopy(indeks, fakta)
            }

        private val prioritet = listOf(
            GrunnleggendeFaktum::class.java,
            TemplateFaktum::class.java,
            GeneratorFaktum::class.java,
            UtledetFaktum::class.java,
        )
    }

    fun <R> reflection(block: (Int, Int) -> R) = faktumId.reflection(block)

    abstract fun type(): Class<R>

    internal abstract fun bygg(byggetFakta: MutableMap<FaktumId, Faktum<*>>): Faktum<*>

    open fun besvar(verdi: R, besvarer: String? = null): Faktum<R> =
        this.also { avhengigeFakta.forEach { it.tilUbesvart() } }

    open fun rehydrer(r: R, besvarer: String?): Faktum<R> = this

    open fun tilUbesvart() {
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

    internal open infix fun leggTilAvhengighet(other: Faktum<*>) {
        this.avhengigeFakta.add(other)
        other.avhengerAvFakta.add(this)
    }

    internal open fun deepCopy(indeks: Int, fakta: Fakta): Faktum<*> = this

    enum class FaktumTilstand {
        Ukjent,
        Kjent,
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
        if (!prioritet.contains(faktum::class.java)) {
            throw Exception("Mangler prioritet for ${faktum::class.simpleName}")
        } else {
            prioritet.indexOf(faktum::class.java)
        }

    internal fun leggTilAvhengigheter(fakta: MutableSet<Faktum<*>>) {
        fakta.addAll(avhengerAvFakta)
    }

    internal fun sjekkAvhengigheter() {
        if (avhengerAvFakta.isEmpty()) {
            throw IllegalArgumentException("Mangler avhengighet på godkjenningsfaktum: $this")
        }
    }

    fun harRolle(rolle: Rolle) = rolle in roller
    fun harIkkeRolle(rolle: Rolle) = !harRolle(rolle)
    internal fun sannsynliggjøresAv(sannsynliggjøringer: Collection<Faktum<*>>) =
        sannsynliggjøringer.forEach {
            require(this.avhengigeFakta.contains(it)) { "${it.navn} (${it.faktumId}) må være avhengig av ${this.navn} (${this.faktumId})" }
        }
}
