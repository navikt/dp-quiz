package no.nav.dagpenger.model.factory

import no.nav.dagpenger.model.faktum.Dokument
import no.nav.dagpenger.model.faktum.Envalg
import no.nav.dagpenger.model.faktum.Faktum
import no.nav.dagpenger.model.faktum.FaktumId
import no.nav.dagpenger.model.faktum.Flervalg
import no.nav.dagpenger.model.faktum.GeneratorFaktum
import no.nav.dagpenger.model.faktum.GrunnleggendeFaktum
import no.nav.dagpenger.model.faktum.GyldigeValg
import no.nav.dagpenger.model.faktum.Inntekt
import no.nav.dagpenger.model.faktum.Land
import no.nav.dagpenger.model.faktum.Periode
import no.nav.dagpenger.model.faktum.Tekst
import no.nav.dagpenger.model.faktum.TemplateFaktum
import java.time.LocalDate

class BaseFaktumFactory<T : Comparable<T>> internal constructor(
    private val clazz: Class<T>,
    private val navn: String,
) : FaktumFactory<T>() {
    private val templateIder = mutableListOf<Int>()
    private val gyldigeValg = mutableSetOf<String>()
    private val landGrupper = mutableMapOf<String, List<Land>>()
    private var navngittAv: Int? = null

    companion object {
        object boolsk {
            infix fun faktum(navn: String) = BaseFaktumFactory(Boolean::class.java, navn)
        }

        object heltall {
            infix fun faktum(navn: String) = BaseFaktumFactory(Int::class.java, navn)
        }

        object desimaltall {
            infix fun faktum(navn: String) = BaseFaktumFactory(Double::class.java, navn)
        }

        object dokument {
            infix fun faktum(navn: String) = BaseFaktumFactory(Dokument::class.java, navn)
        }

        object inntekt {
            infix fun faktum(navn: String) = BaseFaktumFactory(Inntekt::class.java, navn)
        }

        object dato {
            infix fun faktum(navn: String) = BaseFaktumFactory(LocalDate::class.java, navn)
        }

        object envalg {
            infix fun faktum(navn: String) = BaseFaktumFactory(Envalg::class.java, navn)
        }

        object flervalg {
            infix fun faktum(navn: String) = BaseFaktumFactory(Flervalg::class.java, navn)
        }

        object tekst {
            infix fun faktum(navn: String) = BaseFaktumFactory(Tekst::class.java, navn)
        }

        object periode {
            infix fun faktum(navn: String) = BaseFaktumFactory(Periode::class.java, navn)
        }

        object land {
            infix fun faktum(navn: String) = BaseFaktumFactory(Land::class.java, navn)
        }
    }

    infix fun id(rootId: Int) = this.apply { this.rootId = rootId }

    infix fun med(valg: String) = this.apply { gyldigeValg.add("$navn.$valg") }

    infix fun gruppe(gruppeNavn: String) = LandGruppe(this, gruppeNavn)

    class LandGruppe<T : Comparable<T>>(
        private val baseFaktumFactory: BaseFaktumFactory<T>,
        private val gruppeNavn: String,
    ) {
        infix fun med(land: Collection<Land>): BaseFaktumFactory<T> {
            return baseFaktumFactory.landGruppe(
                Pair(gruppeNavn, land.toList()),
            )
        }
    }

    private fun landGruppe(landGruppe: Pair<String, List<Land>>): BaseFaktumFactory<T> {
        return this.apply { landGrupper["${this.navn}.gruppe.${landGruppe.first}"] = landGruppe.second }
    }

    @Suppress("UNCHECKED_CAST")
    override fun faktum(): Faktum<T> {
        return when (clazz) {
            Envalg::class.java -> GrunnleggendeFaktum(
                faktumId = faktumId,
                navn = navn,
                clazz = clazz,
                gyldigeValg = GyldigeValg(gyldigeValg),
            ) as Faktum<T>

            Flervalg::class.java -> GrunnleggendeFaktum(
                faktumId = faktumId,
                navn = navn,
                clazz = clazz,
                gyldigeValg = GyldigeValg(gyldigeValg),
            ) as Faktum<T>

            Land::class.java -> {
                require(landGrupper.isNotEmpty()) { "Kan ikke lage landfaktum $navn uten noen grupper" }
                GrunnleggendeFaktum(
                    faktumId = faktumId,
                    navn = navn,
                    clazz = clazz,
                    landGrupper = landGrupper,
                ) as Faktum<T>
            }

            else -> GrunnleggendeFaktum(faktumId = faktumId, navn = navn, clazz = clazz)
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun og(otherId: Int): FaktumFactory<T> =
        if (templateIder.isEmpty()) {
            super.og(otherId)
        } else {
            genererer(otherId) as FaktumFactory<T>
        }

    @Suppress("UNCHECKED_CAST")
    override infix fun genererer(otherId: Int): BaseFaktumFactory<Int> = (this as BaseFaktumFactory<Int>)
        .also { templateIder.add(otherId) }

    override fun tilTemplate(faktumMap: MutableMap<FaktumId, Faktum<*>>) {
        if (templateIder.isEmpty()) return
        templateIder.forEach { otherId ->
            faktumMap[FaktumId(otherId)]
                ?.tilTemplate()
                ?.also { template -> faktumMap[FaktumId(otherId)] = template }
                ?: throw IllegalArgumentException("Faktum $otherId finnes ikke")
        }
        val navngittAvFaktumId = navngittAv?.let {
            FaktumId(it).also { faktumId ->
                require(faktumMap[faktumId]?.type() == Tekst::class.java) { "navngittAv må være av type tekst" }
            }
        }
        GeneratorFaktum(
            faktumId,
            navn,
            templateIder.map { otherId -> faktumMap[FaktumId(otherId)] as TemplateFaktum<*> },
            navngittAv = navngittAvFaktumId,
        )
            .also { generatorfaktum -> faktumMap[faktumId] = generatorfaktum }
    }

    infix fun navngittAv(otherId: Int): BaseFaktumFactory<*> = this.also {
        navngittAv = otherId
    }

    private val faktumId get() = FaktumId(rootId).also { require(rootId > 0) { "Root id må være positiv" } }
}
