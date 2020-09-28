package no.nav.dagpenger.model.fakta

import no.nav.dagpenger.model.søknad.Seksjon

class GeneratorFaktum(navn: FaktumNavn, private val templates: List<TemplateFaktum<*>>): GrunnleggendeFaktum<Int>(navn, Int::class.java) {
    private val seksjoner = mutableListOf<Seksjon>()

    override fun besvar(r: Int, rolle: Rolle): GrunnleggendeFaktum<Int> {
        super.besvar(r, rolle)
        seksjoner.forEach { seksjon ->
            (0..r).forEach {
                seksjon.add(this)
            }
        }

    }

    override fun add(seksjon: Seksjon) = seksjoner.add(seksjon)

}
