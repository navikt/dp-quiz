package no.nav.dagpenger.model.fakta

import no.nav.dagpenger.model.søknad.Seksjon
import no.nav.dagpenger.model.søknad.Søknad
import no.nav.dagpenger.model.visitor.FaktumVisitor

interface Faktum<R : Comparable<R>> {
    val navn: FaktumNavn
    val id: String get() = navn.id
    val avhengigeFakta: MutableSet<Faktum<*>>

    fun clazz(): Class<R>

    fun besvar(r: R, rolle: Rolle = Rolle.søker): Faktum<R> = this.also {
        avhengigeFakta.forEach {
            it.tilUbesvart()
        }
    }
    fun tilUbesvart()
    fun svar(): R
    fun grunnleggendeFakta(): Set<GrunnleggendeFaktum<*>>
    fun leggTilHvis(kode: FaktumTilstand, fakta: MutableSet<GrunnleggendeFaktum<*>>)
    fun erBesvart(): Boolean
    fun accept(visitor: FaktumVisitor)
    fun add(rolle: Rolle): Boolean
    fun add(seksjon: Seksjon): Boolean = false
    infix fun avhengerAv(other: Faktum<*>) {
        other.avhengigeFakta.add(this)
    }

    fun faktaMap(): Map<FaktumNavn, Faktum<*>>
    open fun med(indeks: Int, søknad: Søknad): Faktum<*> = this

    enum class FaktumTilstand {
        Ukjent,
        Kjent
    }
}

fun <R : Comparable<R>> Collection<Faktum<R>>.faktum(navn: FaktumNavn, regel: FaktaRegel<R>): Faktum<R> =
    UtledetFaktum(navn, this.toSet(), regel)

fun Set<Faktum<*>>.erBesvart() = this.all { it.erBesvart() }
typealias FaktaRegel <R> = (UtledetFaktum<R>) -> R

fun <R : Comparable<R>> FaktumNavn.faktum(clazz: Class<R>) = GrunnleggendeFaktum<R>(this, clazz)

fun <R : Comparable<R>> FaktumNavn.faktum(clazz: Class<R>, vararg templates: TemplateFaktum<*>) = GeneratorFaktum(this, templates.asList())

fun <R : Comparable<R>> FaktumNavn.template(clazz: Class<R>) = TemplateFaktum<R>(this, clazz)

internal fun Set<Faktum<*>>.deepCopy(søknad: Søknad): Set<Faktum<*>> = this
    .mapNotNull { template ->
        søknad.faktum(template.navn)?.also {
            template.deepCopyAvhengigheter(it, søknad)
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

private fun Faktum<*>.deepCopyAvhengigheter(faktum: Faktum<*>, søknad: Søknad) {
    faktum.avhengigeFakta.addAll(this.avhengigeFakta.map { søknad.faktum(it.navn) })
}
