package no.nav.dagpenger.model.subsumsjon

import no.nav.dagpenger.model.faktagrupper.Faktagrupper
import no.nav.dagpenger.model.faktum.Dokument
import no.nav.dagpenger.model.faktum.Faktum
import no.nav.dagpenger.model.faktum.GrunnleggendeFaktum
import no.nav.dagpenger.model.faktum.Søknad
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

    override fun bygg(søknad: Søknad) = AvSubsumsjon(
        regel.bygg(søknad),
        søknad.dokument(dokument.faktumId),
        søknad.ja(godkjenning.faktumId),
        gyldigSubsumsjon.bygg(søknad),
        ugyldigSubsumsjon.bygg(søknad)
    )

    override fun deepCopy() = AvSubsumsjon(
        regel,
        dokument,
        godkjenning,
        gyldigSubsumsjon.deepCopy(),
        ugyldigSubsumsjon.deepCopy()
    )

    override fun deepCopy(indeks: Int, søknad: Søknad) = AvSubsumsjon(
        regel.deepCopy(indeks, søknad),
        dokument.deepCopy(indeks, søknad) as Faktum<Dokument>,
        godkjenning.deepCopy(indeks, søknad) as Faktum<Boolean>,
        gyldigSubsumsjon.deepCopy(indeks, søknad),
        ugyldigSubsumsjon.deepCopy(indeks, søknad)
    )
}
