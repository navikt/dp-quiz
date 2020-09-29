package no.nav.dagpenger.model.søknad

import no.nav.dagpenger.model.fakta.Faktum
import no.nav.dagpenger.model.fakta.FaktumNavn
import no.nav.dagpenger.model.fakta.GrunnleggendeFaktum
import no.nav.dagpenger.model.fakta.Rolle
import no.nav.dagpenger.model.fakta.TemplateFaktum
import no.nav.dagpenger.model.visitor.SøknadVisitor

class Seksjon private constructor(private val rolle: Rolle, private val fakta: MutableSet<Faktum<*>>) : MutableSet<Faktum<*>> by fakta {
    private lateinit var søknad: Søknad

    init {
        fakta.forEach {
            it.add(rolle)
            it.add(this)
        }
    }

    constructor(rolle: Rolle, vararg fakta: Faktum<*>): this(rolle, fakta.toMutableSet())

    internal operator fun contains(nesteFakta: Set<GrunnleggendeFaktum<*>>): Boolean {
        return nesteFakta.any { it in fakta }
    }

    internal fun søknad(søknad: Søknad) { this.søknad = søknad }

    internal fun bareTemplates() = fakta.all { it is TemplateFaktum }

    internal fun deepCopy(indeks: Int): Seksjon = Seksjon(rolle).also {
        søknad.add(
                søknad.indexOf(this) + indeks,
                it
        )
    }

    fun accept(visitor: SøknadVisitor) {
        visitor.preVisit(this, rolle, fakta)
        fakta.forEach { it.accept(visitor) }
        visitor.postVisit(this, rolle)
    }

    internal fun faktaMap(): Map<FaktumNavn, Faktum<*>> {
        return fakta.fold(mapOf()) { resultater, faktum ->
            resultater + faktum.faktaMap()
        }
    }
}
