package no.nav.dagpenger.model.faktum

class ValgFaktum internal constructor(
    faktumId: FaktumId,
    navn: String,
    private val gyldigeValg: Valg,
    avhengigeFakta: MutableSet<Faktum<*>> = mutableSetOf(),
    avhengerAvFakta: MutableSet<Faktum<*>> = mutableSetOf(),
    roller: MutableSet<Rolle> = mutableSetOf(),
) : GrunnleggendeFaktum<Valg>(
    faktumId,
    navn,
    Valg::class.java,
    avhengigeFakta,
    avhengerAvFakta,
    mutableSetOf(),
    roller
) {
    override fun besvar(valg: Valg, besvarer: String?): GrunnleggendeFaktum<Valg> {
        return if (valg.all {
            it in gyldigeValg
        }
        ) {
            super.besvar(valg, besvarer)
        } else throw IllegalArgumentException("Valg $valg er ikke gyldig")
    }

    override fun bygg(byggetFakta: MutableMap<FaktumId, Faktum<*>>): ValgFaktum {
        if (byggetFakta.containsKey(faktumId)) return byggetFakta[faktumId] as ValgFaktum
        return ValgFaktum(
            faktumId = faktumId,
            navn = navn,
            gyldigeValg = gyldigeValg,
            avhengigeFakta = mutableSetOf(),
            avhengerAvFakta = mutableSetOf(),
            roller = roller
        ).also { nyttFaktum ->
            byggetFakta[faktumId] = nyttFaktum
            this.avhengigeFakta.forEach { nyttFaktum.avhengigeFakta.add(it.bygg(byggetFakta)) }
            this.avhengerAvFakta.forEach { nyttFaktum.avhengerAvFakta.add(it.bygg(byggetFakta)) }
            this.godkjenner.forEach { nyttFaktum.godkjenner.add(it.bygg(byggetFakta)) }
        }
    }
}
