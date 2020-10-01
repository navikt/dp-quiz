package no.nav.dagpenger.regelverk

import no.nav.dagpenger.model.fakta.FaktumNavn
import no.nav.dagpenger.model.fakta.Inntekt
import no.nav.dagpenger.model.fakta.faktum
import no.nav.dagpenger.model.regel.MAKS_DATO
import java.time.LocalDate

val fødselsdato get() = FaktumNavn(1, "Fødselsdato").faktum(LocalDate::class.java)
val ønsketDato get() = FaktumNavn(2, "Ønsker dagpenger fra dato").faktum(LocalDate::class.java)
val datoForBortfallPgaAlder get() = FaktumNavn(3, "Dato for bortfall på grunn av alder").faktum(LocalDate::class.java)
val sisteDagMedArbeidsplikt get() = FaktumNavn(5, "Siste dag med arbeidsplikt").faktum(LocalDate::class.java)
val registreringsdato get() = FaktumNavn(6, "Virkningstidspunkt").faktum(LocalDate::class.java)
val sisteDagMedLønn get() = FaktumNavn(7, "Siste dag med lønn").faktum(LocalDate::class.java)

val virkningstidspunkt
    get() =
        setOf(ønsketDato, sisteDagMedLønn, sisteDagMedArbeidsplikt, registreringsdato).faktum(
            FaktumNavn(4, "Virkningstidspunkt"),
            MAKS_DATO
        )

val inntektSiste3år get() = FaktumNavn(8, "Inntekt siste 3 år").faktum(Inntekt::class.java)
val inntektSisteÅr get() = FaktumNavn(9, "Inntekt siste 12 mnd").faktum(Inntekt::class.java)
val dimisjonsdato get() = FaktumNavn(10, "Dimisjonsdato").faktum(LocalDate::class.java)
val inntekt3G get() = FaktumNavn(11, "3G").faktum(Inntekt::class.java)
val inntekt15G get() = FaktumNavn(12, "1,5G").faktum(Inntekt::class.java)

val egenBondegård get() = FaktumNavn(13, "Eier egen bondegård").faktum(Boolean::class.java)
val egenBedrift get() = FaktumNavn(14, "Eier egen bedrift").faktum(Boolean::class.java)
val fangstOgFisk get() = FaktumNavn(15, "Driver med fangst og fisk").faktum(Boolean::class.java)

val villigDeltid get() = FaktumNavn(16, "Villig til deltidsarbeid").faktum(Boolean::class.java)
val villigPendle get() = FaktumNavn(17, "Villig til pendling").faktum(Boolean::class.java)
val villigHelse get() = FaktumNavn(18, "Villig til helse").faktum(Boolean::class.java)
val villigJobb get() = FaktumNavn(19, "Villig til å ta ethvert arbeid").faktum(Boolean::class.java)
