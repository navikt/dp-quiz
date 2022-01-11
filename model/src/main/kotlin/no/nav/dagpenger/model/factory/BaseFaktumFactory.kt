package no.nav.dagpenger.model.factory

import no.nav.dagpenger.model.faktum.Dokument
import no.nav.dagpenger.model.faktum.Faktum
import no.nav.dagpenger.model.faktum.FaktumId
import no.nav.dagpenger.model.faktum.GeneratorFaktum
import no.nav.dagpenger.model.faktum.GrunnleggendeFaktum
import no.nav.dagpenger.model.faktum.Inntekt
import no.nav.dagpenger.model.faktum.TemplateFaktum
import no.nav.dagpenger.model.faktum.Valg
import java.time.LocalDate

class BaseFaktumFactory<T : Comparable<T>> internal constructor(
    private val clazz: Class<T>,
    private val navn: String,
    private val erValgFaktum: Boolean = false
) : FaktumFactory<T>() {
    private val templateIder = mutableListOf<Int>()
    private val gyldigevalg = mutableSetOf<String>()

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

        object flervalg {
            infix fun faktum(navn: String) = BaseFaktumFactory(Valg::class.java, navn, erValgFaktum = true)
        }
    }

    infix fun id(rootId: Int) = this.apply { this.rootId = rootId }

    infix fun med(valg: String) = this.apply { gyldigevalg.add(valg) }

    override fun faktum(): Faktum<T> {
        return if(erValgFaktum) {
            require(gyldigevalg.isNotEmpty()) { "Et valgfaktum uten predefinerte valg?" }
            GrunnleggendeFaktum(faktumId = faktumId, navn = navn, clazz = clazz, gyldigevalg = Valg(gyldigevalg))
        } else {
            GrunnleggendeFaktum(faktumId, navn, clazz)
        }
    }

    fun faktum(vararg templates: TemplateFaktum<*>) = GeneratorFaktum(faktumId, navn, templates.asList())

    override fun og(otherId: Int): FaktumFactory<T> =
        if (templateIder.isEmpty()) super.og(otherId)
        else genererer(otherId) as FaktumFactory<T>

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
        GeneratorFaktum(faktumId, navn, templateIder.map { otherId -> faktumMap[FaktumId(otherId)] as TemplateFaktum<*> })
            .also { generatorfaktum -> faktumMap[faktumId] = generatorfaktum }
    }

    private val faktumId get() = FaktumId(rootId).also { require(rootId > 0) { "Root id må være positiv" } }
}
