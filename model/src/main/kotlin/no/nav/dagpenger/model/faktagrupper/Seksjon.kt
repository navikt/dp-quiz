package no.nav.dagpenger.model.faktagrupper

import no.nav.dagpenger.model.faktum.Faktum
import no.nav.dagpenger.model.faktum.GrunnleggendeFaktum
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.faktum.TemplateFaktum
import no.nav.dagpenger.model.visitor.FaktagruppeVisitor

class Seksjon private constructor(
    val navn: String,
    private val rolle: Rolle,
    private val seksjonFakta: MutableSet<Faktum<*>>
) : MutableSet<Faktum<*>> by seksjonFakta {
    internal lateinit var faktagrupper: Faktagrupper
    private val genererteSeksjoner = mutableListOf<Seksjon>()

    init {
        seksjonFakta.toSet().forEach {
            it.add(rolle)
            it.add(this)
            it.leggTilAvhengigheter(seksjonFakta)
        }
    }

    constructor(navn: String, rolle: Rolle, vararg fakta: Faktum<*>) : this(navn, rolle, fakta.toMutableSet())

    internal operator fun contains(nesteFakta: Set<GrunnleggendeFaktum<*>>): Boolean {
        return nesteFakta.any { it in seksjonFakta }
    }

    internal fun faktagrupper(faktagrupper: Faktagrupper) {
        this.faktagrupper = faktagrupper
    }

    internal fun bareTemplates() = seksjonFakta.all { it is TemplateFaktum }

    internal fun deepCopy(indeks: Int, søknad: Søknad): Seksjon {
        return if (indeks <= genererteSeksjoner.size) genererteSeksjoner[indeks - 1]
        else Seksjon(navn, rolle, mutableSetOf()).also {
            faktagrupper.add(faktagrupper.indexOf(this) + indeks, it)
            genererteSeksjoner.add(it)
            it.faktagrupper(this.faktagrupper)
        }
    }

    fun accept(visitor: FaktagruppeVisitor) {
        visitor.preVisit(this, rolle, seksjonFakta)
        seksjonFakta.forEach { it.accept(visitor) }
        visitor.postVisit(this, rolle)
    }

    internal fun add(faktum: GrunnleggendeFaktum<*>): Boolean =
        faktagrupper.idOrNull(faktum.faktumId).let { eksisterendeFaktum ->
            (eksisterendeFaktum == null).also {
                if (it) { // Use existing Faktum
                    seksjonFakta.add(faktum)
                    faktagrupper.add(faktum)
                } else { // Use new Faktum
                    seksjonFakta.add(eksisterendeFaktum as GrunnleggendeFaktum<*>)
                }
            }
        }

    internal fun bygg(søknad: Søknad) = Seksjon(navn, rolle, this.seksjonFakta.map { søknad.id(it.faktumId) }.toMutableSet())
}
