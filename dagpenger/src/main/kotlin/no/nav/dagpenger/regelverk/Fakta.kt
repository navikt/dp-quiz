package no.nav.dagpenger.regelverk

import no.nav.dagpenger.model.fakta.Faktum
import no.nav.dagpenger.model.fakta.Inntekt
import java.time.LocalDate

val fødselsdato = Faktum<LocalDate>("Fødselsdato")
val datoForBortfallPgaAlder = Faktum<LocalDate>("Dato for bortfall på grunn av alder")
val virkningstidspunkt = Faktum<LocalDate>("Virkningstidspunkt")

val utestengt = Faktum<Boolean>("Er utestengt")

val oppholdINorge = Faktum<Boolean>("Har opphold i Norge")

val sluttårsak = Faktum<Sluttårsak>("Sluttårsak")

val inntektSiste3år = Faktum<Inntekt>("Inntekt siste 3 år")
val inntektSisteÅr = Faktum<Inntekt>("Inntekt siste 12 mnd")
val dimisjonsdato = Faktum<LocalDate>("Dimisjonsdato")
val inntekt3G = Faktum<Inntekt>("3G")
val inntekt15G = Faktum<Inntekt>("1,5G")

enum class Sluttårsak {
    SagtOpp,
    Oppsagt,
    Avskjediget,
    Permittert,
    KontraktUtgått,
    Konkurs,
    RedusertArbeidstid
}
