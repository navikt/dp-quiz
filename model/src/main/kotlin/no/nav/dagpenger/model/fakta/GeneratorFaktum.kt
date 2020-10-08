package no.nav.dagpenger.model.fakta

import no.nav.dagpenger.model.visitor.FaktumVisitor

class GeneratorFaktum(navn: FaktumNavn<Int>, private val templates: List<TemplateFaktum<*>>) : GrunnleggendeFaktum<Int>(navn, Int::class.java) {

    override fun besvar(r: Int, rolle: Rolle): GrunnleggendeFaktum<Int> {
        super.besvar(r, rolle)
        templates.forEach { template ->
            template.generate(r)
        }
        return this
    }

    override fun acceptUtenSvar(visitor: FaktumVisitor) {
        visitor.visit(this, id, avhengigeFakta, templates, roller, Int::class.java)
    }

    override fun acceptMedSvar(visitor: FaktumVisitor) {
        visitor.visit(this, id, avhengigeFakta, templates, roller, Int::class.java, gjeldendeSvar)
    }
}
