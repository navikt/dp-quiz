package no.nav.dagpenger.model.faktum

import no.nav.dagpenger.model.visitor.FaktumVisitor

class GeneratorFaktum internal constructor(
    faktumId: FaktumId,
    navn: String,
    private val templates: List<TemplateFaktum<*>>,
    avhengigeFakta: MutableSet<Faktum<*>> = mutableSetOf(),
    avhengerAvFakta: MutableSet<Faktum<*>> = mutableSetOf(),
    roller: MutableSet<Rolle> = mutableSetOf(),
    private val navngittAv: FaktumId?
) : GrunnleggendeFaktum<Int>(
    faktumId = faktumId,
    navn = navn,
    clazz = Int::class.java,
    avhengigeFakta = avhengigeFakta,
    avhengerAvFakta = avhengerAvFakta,
    godkjenner = mutableSetOf(),
    roller = roller
) {
    internal lateinit var fakta: Fakta

    fun identitet(faktumId: FaktumId): Faktum<Tekst>? {
        val identitetInstans = navngittAv?.medIndeks(faktumId.reflection { _, indeks -> indeks })

        return fakta.filter { isT<Faktum<Tekst>>(it) }.find { it.faktumId == identitetInstans } as Faktum<Tekst>?
    }

    private inline fun <reified T> isT(x: Any) = x is T

    override fun besvar(antall: Int, ident: String?): GrunnleggendeFaktum<Int> {
        if (erBesvart() && svar() == antall) {
            return this
        }

        tilbakestill()
        super.besvar(antall, ident)
        templates.forEach { template -> template.generate(antall, fakta) }
        return this
    }

    internal fun harGenerert(other: FaktumId) = this.templates.any {
        other.generertFra(it.faktumId)
    }

    override fun rehydrer(r: Int, besvarer: String?): Faktum<Int> = this.also {
        super.rehydrer(r, besvarer)
        templates.forEach { template -> template.generate(r, fakta) }
    }

    override fun tilUbesvart() {
        tilbakestill()
        super.tilUbesvart()
    }

    private fun tilbakestill() {
        templates.forEach { template -> template.tilbakestill() }
        fakta.removeAll(templates)
    }

    override fun acceptUtenSvar(visitor: FaktumVisitor) {
        visitor.visitUtenSvar(this, id, avhengigeFakta, avhengerAvFakta, templates, roller, Int::class.java)
    }

    override fun acceptMedSvar(visitor: FaktumVisitor) {
        val genererteFaktum =
            templates.flatMap { template -> fakta.filter { it.faktumId.generertFra(template.faktumId) && it.erBesvart() } }
                .toSet()
        visitor.visitMedSvar(
            this,
            id,
            avhengigeFakta,
            avhengerAvFakta,
            templates,
            roller,
            Int::class.java,
            gjeldendeSvar,
            genererteFaktum
        )
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
            roller,
            navngittAv
        ).also { nyttFaktum ->
            byggetFakta[faktumId] = nyttFaktum
            this.avhengigeFakta.forEach { nyttFaktum.avhengigeFakta.add(it.bygg(byggetFakta)) }
            this.avhengerAvFakta.forEach { nyttFaktum.avhengerAvFakta.add(it.bygg(byggetFakta)) }
            this.godkjenner.forEach { nyttFaktum.godkjenner.add(it.bygg(byggetFakta)) }
        }
    }
}
