package no.nav.dagpenger.model.subsumsjon

import no.nav.dagpenger.model.faktum.Dokument
import no.nav.dagpenger.model.faktum.Faktum
import no.nav.dagpenger.model.faktum.GrunnleggendeFaktum
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.regel.Regel
import no.nav.dagpenger.model.seksjon.Søknadprosess

internal class AvSubsumsjon private constructor(
    regel: Regel,
    private val dokument: Faktum<Dokument>,
    private val godkjenning: Faktum<Boolean>,
    gyldigSubsumsjon: Subsumsjon,
    ugyldigSubsumsjon: Subsumsjon
) : EnkelSubsumsjon(
    regel,
    listOf(dokument, godkjenning),
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

    override fun deepCopy(søknadprosess: Søknadprosess) = AvSubsumsjon(
        regel.deepCopy(søknadprosess),
        søknadprosess.faktum(dokument.faktumId) as Faktum<Dokument>,
        søknadprosess.faktum(godkjenning.faktumId) as Faktum<Boolean>,
        gyldigSubsumsjon.deepCopy(søknadprosess),
        ugyldigSubsumsjon.deepCopy(søknadprosess)
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
