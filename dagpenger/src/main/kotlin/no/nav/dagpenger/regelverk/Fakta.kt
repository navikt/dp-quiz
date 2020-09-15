package no.nav.dagpenger.regelverk

import no.nav.dagpenger.model.unit.fakta.Inntekt
import no.nav.dagpenger.model.unit.fakta.faktum
import java.time.LocalDate

val fødselsdato = "Fødselsdato".faktum<LocalDate>()
val datoForBortfallPgaAlder = "Dato for bortfall på grunn av alder".faktum<LocalDate>()
val virkningstidspunkt = "Virkningstidspunkt".faktum<LocalDate>()

val utestengt = "Er utestengt".faktum<Boolean>()

val oppholdINorge = "Har opphold i Norge".faktum<Boolean>()

val sluttårsak = "Sluttårsak".faktum<Sluttårsak>()

val inntektSiste3år = "Inntekt siste 3 år".faktum<Inntekt>()
val inntektSisteÅr = "Inntekt siste 12 mnd".faktum<Inntekt>()
val dimisjonsdato = "Dimisjonsdato".faktum<LocalDate>()
val inntekt3G = "3G".faktum<Inntekt>()
val inntekt15G = "1,5G".faktum<Inntekt>()

enum class Sluttårsak {
    SagtOpp,
    Oppsagt,
    Avskjediget,
    Permittert,
    KontraktUtgått,
    Konkurs,
    RedusertArbeidstid
}
