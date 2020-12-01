package no.nav.dagpenger.model.faktum

import no.nav.dagpenger.model.visitor.FaktumVisitor

class GeneratorFaktum internal constructor(
    faktumId: FaktumId,
    navn: String,
    private val templates: List<TemplateFaktum<*>>,
    avhengigeFakta: MutableSet<Faktum<*>> = mutableSetOf(),
    avhengerAvFakta: MutableSet<Faktum<*>> = mutableSetOf(),
    roller: MutableSet<Rolle> = mutableSetOf()
) : GrunnleggendeFaktum<Int>(
    faktumId,
    navn,
    Int::class.java,
    avhengigeFakta,
    avhengerAvFakta,
    mutableSetOf(),
    roller
) {
    internal lateinit var søknad: Søknad

    override fun besvar(r: Int): GrunnleggendeFaktum<Int> = this.also {
        if (erBesvart() && svar() != r) tilbakestill()
        super.besvar(r)
        templates.forEach { template -> template.generate(r, søknad) }
    }

    private fun tilbakestill() {
        templates.forEach { template -> template.tilbakestill() }
        søknad.removeAll(templates)
    }

    override fun acceptUtenSvar(visitor: FaktumVisitor) {
        visitor.visit(this, id, avhengigeFakta, avhengerAvFakta, templates, roller, Int::class.java)
    }

    override fun acceptMedSvar(visitor: FaktumVisitor) {
        visitor.visit(this, id, avhengigeFakta, avhengerAvFakta, templates, roller, Int::class.java, gjeldendeSvar)
    }

    override fun bygg(byggetFakta: MutableMap<FaktumId, Faktum<*>>): GeneratorFaktum {
        if (byggetFakta.containsKey(faktumId)) return byggetFakta[faktumId] as GeneratorFaktum
        val avhengigheter = avhengigeFakta.map { it.bygg(byggetFakta) }.toMutableSet()
        val templates = templates.map { it.bygg(byggetFakta) as TemplateFaktum<*> }
        return GeneratorFaktum(
            faktumId,
            navn,
            templates,
            avhengigheter,
            avhengerAvFakta,
            roller
        ).also { byggetFakta[faktumId] = it }
    }
}
