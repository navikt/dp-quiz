package no.nav.dagpenger.regelverk

import no.nav.dagpenger.Dagpengefakta
import no.nav.dagpenger.model.regel.er
import no.nav.dagpenger.model.regel.før
import no.nav.dagpenger.model.regel.ikkeFør
import no.nav.dagpenger.model.regel.minst
import no.nav.dagpenger.model.subsumsjon.alle
import no.nav.dagpenger.model.subsumsjon.minstEnAv
import no.nav.dagpenger.model.subsumsjon.så

val inngangsvilkår = with(Dagpengefakta()) {
    "Inngangsvilkår".alle(
        "reell arbeidssøker".alle(
            villigDeltid er true,
            villigPendle er true,
            villigHelse er true,
            villigJobb er true,
            virkningstidspunkt ikkeFør registreringsdato
        ),
        "alder".alle(
            virkningstidspunkt før datoForBortfallPgaAlder,
        ).så(
            "resten".alle(
                "har ikke egen næring".alle(
                    egenBedrift er false,
                    egenBondegård er false,
                    fangstOgFisk er false
                ),
                "minste arbeidsinntekt".minstEnAv(
                    inntektSiste3År minst inntekt3G,
                    inntektSisteÅr minst inntekt15G,
                    dimisjonsdato før virkningstidspunkt
                )
            ),
        ),
    )
}
