package no.nav.dagpenger.model.fakta

import no.nav.dagpenger.model.visitor.FaktumVisitor

class GeneratorFaktum internal constructor(
    faktumId: FaktumId,
    navn: String,
    private val templates: List<TemplateFaktum<*>>,
    avhengigeFakta: MutableSet<Faktum<*>> = mutableSetOf(),
    roller: MutableSet<Rolle> = mutableSetOf()
) : GrunnleggendeFaktum<Int>(
    faktumId,
    navn,
    Int::class.java,
    avhengigeFakta,
    roller
) {
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

    override fun bygg(byggetFakta: MutableMap<FaktumId, Faktum<*>>): GeneratorFaktum {
        if (byggetFakta.containsKey(faktumId)) return byggetFakta[faktumId] as GeneratorFaktum
        val avhengigheter = avhengigeFakta.map { it.bygg(byggetFakta) }.toMutableSet()
        val templates = templates.map { it.bygg(byggetFakta) as TemplateFaktum<*> }
        return GeneratorFaktum(faktumId, navn, templates, avhengigheter, roller).also { byggetFakta[faktumId] = it }
    }
}
