package no.nav.dagpenger.model.faktum

class FlervalgFaktum internal constructor(
    faktumId: FaktumId,
    navn: String,
    private val valg: List<String>,
    avhengigeFakta: MutableSet<Faktum<*>> = mutableSetOf(),
    avhengerAvFakta: MutableSet<Faktum<*>> = mutableSetOf(),
    roller: MutableSet<Rolle> = mutableSetOf(),
) : GrunnleggendeFaktum<String>(
    faktumId,
    navn,
    String::class.java,
    avhengigeFakta,
    avhengerAvFakta,
    mutableSetOf(),
    roller
)
