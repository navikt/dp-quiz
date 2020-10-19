package no.nav.dagpenger.model.fakta

import java.time.LocalDate

class FaktumFactory<T : Comparable<T>> internal constructor(private val clazz: Class<T>, private val navn: String) {
    private var rootId = 0

    companion object {
        object boolean { infix fun faktum(navn: String) = FaktumFactory(Boolean::class.java, navn) }
        object desimal { infix fun faktum(navn: String) = FaktumFactory(Double::class.java, navn) }
        object heltall { infix fun faktum(navn: String) = FaktumFactory(Int::class.java, navn) }
        object dokument { infix fun faktum(navn: String) = FaktumFactory(Dokument::class.java, navn) }
        object inntekt { infix fun faktum(navn: String) = FaktumFactory(Inntekt::class.java, navn) }
        object dato { infix fun faktum(navn: String) = FaktumFactory(LocalDate::class.java, navn) }
    }

    infix fun id(rootId: Int) = this.also { this.rootId = rootId }

    val faktum: Faktum<T> get() = GrunnleggendeFaktum<T>(faktumNavn, clazz)

    fun faktum(vararg templates: TemplateFaktum<*>) = GeneratorFaktum(faktumNavn, templates.asList())

    val template: Faktum<T> get() = TemplateFaktum<T>(faktumNavn, clazz)

    private val faktumNavn get() = FaktumNavn(rootId, navn).also { require(rootId != 0) }
}
