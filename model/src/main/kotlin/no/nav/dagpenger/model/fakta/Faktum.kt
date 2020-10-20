package no.nav.dagpenger.model.fakta

import no.nav.dagpenger.model.søknad.Seksjon
import no.nav.dagpenger.model.søknad.Søknad
import no.nav.dagpenger.model.visitor.FaktumVisitor

abstract class Faktum<R : Comparable<R>>internal constructor(
    internal val faktumId: FaktumId,
    val navn: String,
    protected val avhengigeFakta: MutableSet<Faktum<*>> = mutableSetOf(),
    protected val roller: MutableSet<Rolle> = mutableSetOf()
) {

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

        internal fun Set<Faktum<*>>.deepCopy(indeks: Int, søknad: Søknad): Set<Faktum<*>> = this
            .map { faktum ->
                faktum.med(indeks, søknad)
            }
            .toSet()
    }

    abstract fun clazz(): Class<R>

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

    internal open fun med(indeks: Int, søknad: Søknad): Faktum<*> = this

    enum class FaktumTilstand {
        Ukjent,
        Kjent
    }

    override fun toString() = "$navn med id $id"
}

fun <R : Comparable<R>> Collection<Faktum<R>>.faktum(navn: FaktumNavn, regel: FaktaRegel<R>): Faktum<R> =
    UtledetFaktum(navn.faktumId, navn.navn, this.toMutableSet(), regel)

fun Set<Faktum<*>>.erBesvart() = this.all { it.erBesvart() }
typealias FaktaRegel <R> = (UtledetFaktum<R>) -> R

fun <R : Comparable<R>> FaktumNavn.faktum(clazz: Class<R>) = GrunnleggendeFaktum<R>(this.faktumId, this.navn, clazz)

fun <R : Comparable<R>> FaktumNavn.faktum(clazz: Class<R>, vararg templates: TemplateFaktum<*>) = GeneratorFaktum(this.faktumId, this.navn, templates.asList())

fun <R : Comparable<R>> FaktumNavn.template(clazz: Class<R>) = TemplateFaktum<R>(this.faktumId, this.navn, clazz)
