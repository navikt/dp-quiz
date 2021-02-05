package no.nav.dagpenger.model.faktum

import no.nav.dagpenger.model.visitor.FaktumVisitor

class GeneratorFaktum internal constructor(
    faktumId: FaktumId,
    navn: String,
    private val templates: List<TemplateFaktum<*>>,
    avhengigeFakta: MutableSet<Faktum<*>> = mutableSetOf(),
    avhengerAvFakta: MutableSet<Faktum<*>> = mutableSetOf(),
    roller: MutableSet<Rolle> = mutableSetOf(),
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

    override fun besvar(r: Int, ident: String?): GrunnleggendeFaktum<Int> = this.also {
        if (erBesvart() && svar() != r) tilbakestill()
        super.besvar(r, ident)
        templates.forEach { template -> template.generate(r, søknad) }
    }

    override fun rehydrer(r: Int, besvarer: String?): Faktum<Int> = this.also {
        super.rehydrer(r, besvarer)
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
        val templates = templates.map { it.bygg(byggetFakta) as TemplateFaktum<*> }
        return GeneratorFaktum(
            faktumId,
            navn,
            templates,
            mutableSetOf(),
            mutableSetOf(),
            roller
        ).also { nyttFaktum ->
            byggetFakta[faktumId] = nyttFaktum
            this.avhengigeFakta.forEach { nyttFaktum.avhengigeFakta.add(it.bygg(byggetFakta)) }
            this.avhengerAvFakta.forEach { nyttFaktum.avhengerAvFakta.add(it.bygg(byggetFakta)) }
            this.godkjenner.forEach { nyttFaktum.godkjenner.add(it.bygg(byggetFakta)) }
        }
    }
}
