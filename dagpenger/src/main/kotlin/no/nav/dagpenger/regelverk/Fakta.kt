package no.nav.dagpenger.regelverk

import no.nav.dagpenger.model.fakta.GrunnleggendeFaktum
import no.nav.dagpenger.model.fakta.Inntekt
import java.time.LocalDate

val fødselsdato = GrunnleggendeFaktum<LocalDate>("Fødselsdato")
val datoForBortfallPgaAlder = GrunnleggendeFaktum<LocalDate>("Dato for bortfall på grunn av alder")
val virkningstidspunkt = GrunnleggendeFaktum<LocalDate>("Virkningstidspunkt")

val utestengt = GrunnleggendeFaktum<Boolean>("Er utestengt")

val oppholdINorge = GrunnleggendeFaktum<Boolean>("Har opphold i Norge")

val sluttårsak = GrunnleggendeFaktum<Sluttårsak>("Sluttårsak")

val inntektSiste3år = GrunnleggendeFaktum<Inntekt>("Inntekt siste 3 år")
val inntektSisteÅr = GrunnleggendeFaktum<Inntekt>("Inntekt siste 12 mnd")
val dimisjonsdato = GrunnleggendeFaktum<LocalDate>("Dimisjonsdato")
val inntekt3G = GrunnleggendeFaktum<Inntekt>("3G")
val inntekt15G = GrunnleggendeFaktum<Inntekt>("1,5G")

enum class Sluttårsak {
    SagtOpp,
    Oppsagt,
    Avskjediget,
    Permittert,
    KontraktUtgått,
    Konkurs,
    RedusertArbeidstid
}
