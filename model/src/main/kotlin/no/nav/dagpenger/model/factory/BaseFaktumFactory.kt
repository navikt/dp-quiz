package no.nav.dagpenger.model.factory

import no.nav.dagpenger.model.fakta.Dokument
import no.nav.dagpenger.model.fakta.Faktum
import no.nav.dagpenger.model.fakta.FaktumId
import no.nav.dagpenger.model.fakta.GeneratorFaktum
import no.nav.dagpenger.model.fakta.GrunnleggendeFaktum
import no.nav.dagpenger.model.fakta.Inntekt
import no.nav.dagpenger.model.fakta.TemplateFaktum
import java.time.LocalDate

class BaseFaktumFactory<T : Comparable<T>> internal constructor(
    private val clazz: Class<T>,
    private val navn: String
) : FaktumFactory<T>() {

    companion object {
        object ja { infix fun nei(navn: String) = BaseFaktumFactory(Boolean::class.java, navn) }
        object desimal { infix fun faktum(navn: String) = BaseFaktumFactory(Double::class.java, navn) }
        object heltall { infix fun faktum(navn: String) = BaseFaktumFactory(Int::class.java, navn) }
        object dokument { infix fun faktum(navn: String) = BaseFaktumFactory(Dokument::class.java, navn) }
        object inntekt { infix fun faktum(navn: String) = BaseFaktumFactory(Inntekt::class.java, navn) }
        object dato { infix fun faktum(navn: String) = BaseFaktumFactory(LocalDate::class.java, navn) }
    }

    infix fun id(rootId: Int) = this.also { this.rootId = rootId }

    override fun faktum() = GrunnleggendeFaktum(faktumId, navn, clazz)

    fun faktum(vararg templates: TemplateFaktum<*>) = GeneratorFaktum(faktumId, navn, templates.asList())

    val template: Faktum<T> get() = TemplateFaktum(faktumId, navn, clazz)

    private val faktumId get() = FaktumId(rootId).also { require(rootId > 0) { "Root id må være positiv" } }
}
