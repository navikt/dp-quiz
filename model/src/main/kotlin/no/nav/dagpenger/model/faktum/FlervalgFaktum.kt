package no.nav.dagpenger.model.faktum

class FlervalgFaktum internal constructor(
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
            }) {
            super.besvar(valg, besvarer)
        } else throw IllegalArgumentException("Valg $valg er ikke gyldig")
    }
}
