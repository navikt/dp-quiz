package no.nav.dagpenger.model.søknad

import no.nav.dagpenger.model.fakta.Faktum
import no.nav.dagpenger.model.fakta.FaktumNavn
import no.nav.dagpenger.model.fakta.GrunnleggendeFaktum
import no.nav.dagpenger.model.fakta.Rolle
import no.nav.dagpenger.model.fakta.TemplateFaktum
import no.nav.dagpenger.model.visitor.SøknadVisitor

class Seksjon private constructor(
    val navn: String,
    private val rolle: Rolle,
    private val fakta: MutableSet<Faktum<*>>
) : MutableSet<Faktum<*>> by fakta {
    internal lateinit var søknad: Søknad
    private val genererteSeksjoner = mutableListOf<Seksjon>()

    init {
        fakta.forEach {
            it.add(rolle)
            it.add(this)
        }
    }

    constructor(navn: String, rolle: Rolle, vararg fakta: Faktum<*>) : this(navn, rolle, fakta.toMutableSet())

    internal operator fun contains(nesteFakta: Set<GrunnleggendeFaktum<*>>): Boolean {
        return nesteFakta.any { it in fakta }
    }

    internal fun søknad(søknad: Søknad) {
        this.søknad = søknad
    }

    internal fun bareTemplates() = fakta.all { it is TemplateFaktum }

    internal fun deepCopy(indeks: Int): Seksjon {
        return if (indeks <= genererteSeksjoner.size) genererteSeksjoner[indeks - 1]
        else Seksjon(navn, rolle).also {
            søknad.add(søknad.indexOf(this) + indeks, it)
            genererteSeksjoner.add(it)
            it.søknad(this.søknad)
        }
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

    internal fun add(faktum: GrunnleggendeFaktum<*>): Boolean =
        søknad.fakta[faktum.navn].let { eksisterendeFaktum ->
            (eksisterendeFaktum == null).also {
                if (it) {
                    fakta.add(faktum)
                    søknad.fakta[faktum.navn] = faktum
                } else {
                    fakta.add(eksisterendeFaktum as GrunnleggendeFaktum<*>)
                }
            }
        }
}
