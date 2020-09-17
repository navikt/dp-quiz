package no.nav.dagpenger.regelverk

import no.nav.dagpenger.model.fakta.FaktumNavn
import no.nav.dagpenger.model.fakta.Inntekt
import no.nav.dagpenger.model.fakta.faktum
import java.time.LocalDate

val fødselsdato = FaktumNavn(1, "Fødselsdato").faktum<LocalDate>()
val ønsketDato = FaktumNavn(2, "Ønsker dagpenger fra dato").faktum<LocalDate>()
val datoForBortfallPgaAlder = FaktumNavn(3, "Dato for bortfall på grunn av alder").faktum<LocalDate>()
val virkningstidspunkt = FaktumNavn(4, "Virkningstidspunkt").faktum<LocalDate>()

val utestengt = FaktumNavn(5, "Er utestengt").faktum<Boolean>()

val oppholdINorge = FaktumNavn(6, "Har opphold i Norge").faktum<Boolean>()

val sluttårsak = FaktumNavn(7, "Sluttårsak").faktum<Sluttårsak>()

val inntektSiste3år = FaktumNavn(8, "Inntekt siste 3 år").faktum<Inntekt>()
val inntektSisteÅr = FaktumNavn(9, "Inntekt siste 12 mnd").faktum<Inntekt>()
val dimisjonsdato = FaktumNavn(10, "Dimisjonsdato").faktum<LocalDate>()
val inntekt3G = FaktumNavn(11, "3G").faktum<Inntekt>()
val inntekt15G = FaktumNavn(12, "1,5G").faktum<Inntekt>()

enum class Sluttårsak {
    SagtOpp,
    Oppsagt,
    Avskjediget,
    Permittert,
    KontraktUtgått,
    Konkurs,
    RedusertArbeidstid
}
