package no.nav.dagpenger.regelverk

import no.nav.dagpenger.model.fakta.FaktumNavn
import no.nav.dagpenger.model.fakta.Inntekt
import no.nav.dagpenger.model.fakta.faktum
import java.time.LocalDate

val fødselsdato = FaktumNavn(1, "Fødselsdato").faktum<LocalDate>(LocalDate::class.java)
val ønsketDato = FaktumNavn(2, "Ønsker dagpenger fra dato").faktum<LocalDate>(LocalDate::class.java)
val datoForBortfallPgaAlder = FaktumNavn(3, "Dato for bortfall på grunn av alder").faktum<LocalDate>(LocalDate::class.java)
val virkningstidspunkt = FaktumNavn(4, "Virkningstidspunkt").faktum<LocalDate>(LocalDate::class.java)

val utestengt = FaktumNavn(5, "Er utestengt").faktum<Boolean>(Boolean::class.java)

val oppholdINorge = FaktumNavn(6, "Har opphold i Norge").faktum<Boolean>(Boolean::class.java)

val sluttårsak = FaktumNavn(7, "Sluttårsak").faktum<Sluttårsak>(Sluttårsak::class.java)

val inntektSiste3år = FaktumNavn(8, "Inntekt siste 3 år").faktum<Inntekt>(Inntekt::class.java)
val inntektSisteÅr = FaktumNavn(9, "Inntekt siste 12 mnd").faktum<Inntekt>(Inntekt::class.java)
val dimisjonsdato = FaktumNavn(10, "Dimisjonsdato").faktum<LocalDate>(LocalDate::class.java)
val inntekt3G = FaktumNavn(11, "3G").faktum<Inntekt>(Inntekt::class.java)
val inntekt15G = FaktumNavn(12, "1,5G").faktum<Inntekt>(Inntekt::class.java)

enum class Sluttårsak {
    SagtOpp,
    Oppsagt,
    Avskjediget,
    Permittert,
    KontraktUtgått,
    Konkurs,
    RedusertArbeidstid
}
