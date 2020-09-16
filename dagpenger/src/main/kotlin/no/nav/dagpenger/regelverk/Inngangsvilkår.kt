package no.nav.dagpenger.regelverk

import no.nav.dagpenger.model.regel.erIkke
import no.nav.dagpenger.model.regel.etter
import no.nav.dagpenger.model.regel.før
import no.nav.dagpenger.model.regel.har
import no.nav.dagpenger.model.regel.minst
import no.nav.dagpenger.model.subsumsjon.alle
import no.nav.dagpenger.model.subsumsjon.minstEnAv

val inngangsvilkår = "Inngangsvilkår".alle(
    har(oppholdINorge),
    "tapt arbeidsinntekt".alle(),
    "tapt arbeidstid".alle(),
    "minste arbeidsinntekt".minstEnAv(
        inntektSiste3år minst inntekt3G,
        inntektSisteÅr minst inntekt15G,
        dimisjonsdato etter virkningstidspunkt
    ),
    "reell arbeidssøker".alle(),
    "alder".alle(
        virkningstidspunkt før datoForBortfallPgaAlder
    ),
    erIkke(utestengt)
)
