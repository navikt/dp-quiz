package no.nav.dagpenger.model.fakta

import no.nav.dagpenger.model.visitor.FaktumVisitor

class GeneratorFaktum(faktumId: FaktumId, navn: String, private val templates: List<TemplateFaktum<*>>) : GrunnleggendeFaktum<Int>(faktumId, navn, Int::class.java) {

    override fun besvar(r: Int, rolle: Rolle): GrunnleggendeFaktum<Int> = this.also {
        super.besvar(r, rolle)
        templates.forEach { template -> template.generate(r) }
    }

    override fun acceptUtenSvar(visitor: FaktumVisitor) {
        visitor.visit(this, id, avhengigeFakta, templates, roller, Int::class.java)
    }

    override fun acceptMedSvar(visitor: FaktumVisitor) {
        visitor.visit(this, id, avhengigeFakta, templates, roller, Int::class.java, gjeldendeSvar)
    }
}
