import no.nav.dagpenger.model.regel.erIkke
import no.nav.dagpenger.model.regel.etter
import no.nav.dagpenger.model.regel.før
import no.nav.dagpenger.model.regel.har
import no.nav.dagpenger.model.regel.minst
import no.nav.dagpenger.model.subsumsjon.alle
import no.nav.dagpenger.model.subsumsjon.minstEnAv
import no.nav.dagpenger.regelverk.datoForBortfallPgaAlder
import no.nav.dagpenger.regelverk.dimisjonsdato
import no.nav.dagpenger.regelverk.inntekt15G
import no.nav.dagpenger.regelverk.inntekt3G
import no.nav.dagpenger.regelverk.inntektSiste3år
import no.nav.dagpenger.regelverk.inntektSisteÅr
import no.nav.dagpenger.regelverk.oppholdINorge
import no.nav.dagpenger.regelverk.utestengt
import no.nav.dagpenger.regelverk.virkningstidspunkt

val inngangsvilkår = "inngangsvilkår".alle(
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
