package no.nav.dagpenger.model.søknad

import no.nav.dagpenger.model.fakta.Fakta
import no.nav.dagpenger.model.fakta.Faktum
import no.nav.dagpenger.model.fakta.GrunnleggendeFaktum
import no.nav.dagpenger.model.fakta.Rolle
import no.nav.dagpenger.model.fakta.TemplateFaktum
import no.nav.dagpenger.model.visitor.SøknadVisitor

class Seksjon private constructor(
    val navn: String,
    private val rolle: Rolle,
    private val seksjonFakta: MutableSet<Faktum<*>>
) : MutableSet<Faktum<*>> by seksjonFakta {
    internal lateinit var søknad: Søknad
    private val genererteSeksjoner = mutableListOf<Seksjon>()

    init {
        seksjonFakta.forEach {
            it.add(rolle)
            it.add(this)
        }
    }

    constructor(navn: String, rolle: Rolle, vararg fakta: Faktum<*>) : this(navn, rolle, fakta.toMutableSet())

    internal operator fun contains(nesteFakta: Set<GrunnleggendeFaktum<*>>): Boolean {
        return nesteFakta.any { it in seksjonFakta }
    }

    internal fun søknad(søknad: Søknad) {
        this.søknad = søknad
    }

    internal fun bareTemplates() = seksjonFakta.all { it is TemplateFaktum }

    internal fun deepCopy(indeks: Int, fakta: Fakta): Seksjon {
        return if (indeks <= genererteSeksjoner.size) genererteSeksjoner[indeks - 1]
        else Seksjon(navn, rolle, mutableSetOf()).also {
            søknad.add(søknad.indexOf(this) + indeks, it)
            genererteSeksjoner.add(it)
            it.søknad(this.søknad)
        }
    }

    fun accept(visitor: SøknadVisitor) {
        visitor.preVisit(this, rolle, seksjonFakta)
        seksjonFakta.forEach { it.accept(visitor) }
        visitor.postVisit(this, rolle)
    }

    internal fun add(faktum: GrunnleggendeFaktum<*>): Boolean =
        søknad.idOrNull(faktum.faktumId).let { eksisterendeFaktum ->
            (eksisterendeFaktum == null).also {
                if (it) { // Use existing Faktum
                    seksjonFakta.add(faktum)
                    søknad.add(faktum)
                } else { // Use new Faktum
                    seksjonFakta.add(eksisterendeFaktum as GrunnleggendeFaktum<*>)
                }
            }
        }

    internal fun bygg(fakta: Fakta) = Seksjon(navn, rolle, this.seksjonFakta.map { fakta.id(it.faktumId) }.toMutableSet())
}
