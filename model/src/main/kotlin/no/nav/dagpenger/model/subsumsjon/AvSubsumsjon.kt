package no.nav.dagpenger.model.subsumsjon

import no.nav.dagpenger.model.faktagrupper.Faktagrupper
import no.nav.dagpenger.model.faktum.Dokument
import no.nav.dagpenger.model.faktum.Fakta
import no.nav.dagpenger.model.faktum.Faktum
import no.nav.dagpenger.model.faktum.GrunnleggendeFaktum
import no.nav.dagpenger.model.regel.Regel

internal class AvSubsumsjon private constructor(
    regel: Regel,
    private val dokument: Faktum<Dokument>,
    private val godkjenning: Faktum<Boolean>,
    gyldigSubsumsjon: Subsumsjon,
    ugyldigSubsumsjon: Subsumsjon
) : EnkelSubsumsjon(
    regel,
    setOf(dokument, godkjenning),
    gyldigSubsumsjon,
    ugyldigSubsumsjon
) {

    constructor(regel: Regel, dokument: Faktum<Dokument>, godkjenning: Faktum<Boolean>) : this(
        regel,
        dokument,
        godkjenning,
        TomSubsumsjon,
        TomSubsumsjon
    )

    override fun lokaltResultat(): Boolean? {
        return if (dokument.erBesvart()) regel.resultat() else null
    }

    override fun ukjenteFakta(): Set<GrunnleggendeFaktum<*>> =
        if (dokument.erBesvart()) emptySet() else dokument.grunnleggendeFakta()

    override fun deepCopy(faktagrupper: Faktagrupper) = AvSubsumsjon(
        regel.deepCopy(faktagrupper),
        faktagrupper.faktum(dokument.faktumId) as Faktum<Dokument>,
        faktagrupper.faktum(godkjenning.faktumId) as Faktum<Boolean>,
        gyldigSubsumsjon.deepCopy(faktagrupper),
        ugyldigSubsumsjon.deepCopy(faktagrupper)
    )

    override fun bygg(fakta: Fakta) = AvSubsumsjon(
        regel.bygg(fakta),
        fakta.dokument(dokument.faktumId),
        fakta.ja(godkjenning.faktumId),
        gyldigSubsumsjon.bygg(fakta),
        ugyldigSubsumsjon.bygg(fakta)
    )

    override fun deepCopy() = AvSubsumsjon(
        regel,
        dokument,
        godkjenning,
        gyldigSubsumsjon.deepCopy(),
        ugyldigSubsumsjon.deepCopy()
    )

    override fun deepCopy(indeks: Int, fakta: Fakta) = AvSubsumsjon(
        regel.deepCopy(indeks, fakta),
        dokument.deepCopy(indeks, fakta) as Faktum<Dokument>,
        godkjenning.deepCopy(indeks, fakta) as Faktum<Boolean>,
        gyldigSubsumsjon.deepCopy(indeks, fakta),
        ugyldigSubsumsjon.deepCopy(indeks, fakta)
    )
}
