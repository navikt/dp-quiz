package no.nav.dagpenger.model.fakta

class GeneratorFaktum(navn: FaktumNavn, private val templates: List<TemplateFaktum<*>>) : GrunnleggendeFaktum<Int>(navn, Int::class.java) {

    override fun besvar(r: Int, rolle: Rolle): GrunnleggendeFaktum<Int> {
        super.besvar(r, rolle)
        templates.forEach { template ->
            template.generate(r)
        }
        return this
    }
}
