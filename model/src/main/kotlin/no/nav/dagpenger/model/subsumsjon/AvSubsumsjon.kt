package no.nav.dagpenger.model.subsumsjon

import no.nav.dagpenger.model.fakta.Dokument
import no.nav.dagpenger.model.fakta.Faktum
import no.nav.dagpenger.model.fakta.GrunnleggendeFaktum
import no.nav.dagpenger.model.regel.Regel
import no.nav.dagpenger.model.søknad.Søknad

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

    override fun deepCopy(søknad: Søknad) = AvSubsumsjon(
        regel.deepCopy(søknad),
        søknad.faktum(dokument.navn) as Faktum<Dokument>,
        søknad.faktum(godkjenning.navn) as Faktum<Boolean>,
        gyldigSubsumsjon.deepCopy(søknad),
        ugyldigSubsumsjon.deepCopy(søknad)
    ).also {
        it.søknad = søknad
    }

    override fun deepCopy() = AvSubsumsjon(
        regel,
        dokument,
        godkjenning,
        gyldigSubsumsjon.deepCopy(),
        ugyldigSubsumsjon.deepCopy()
    )

    override fun deepCopy(indeks: Int) = AvSubsumsjon(
        regel.deepCopy(indeks, søknad),
        dokument.med(indeks, søknad) as Faktum<Dokument>,
        godkjenning.med(indeks, søknad) as Faktum<Boolean>,
        gyldigSubsumsjon.deepCopy(indeks),
        ugyldigSubsumsjon.deepCopy(indeks)
    )
}
