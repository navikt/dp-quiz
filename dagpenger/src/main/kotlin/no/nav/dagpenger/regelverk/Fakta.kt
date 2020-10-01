package no.nav.dagpenger.regelverk

import no.nav.dagpenger.model.fakta.FaktumNavn
import no.nav.dagpenger.model.fakta.Inntekt
import no.nav.dagpenger.model.fakta.faktum
import no.nav.dagpenger.model.regel.MAKS_DATO
import java.time.LocalDate

val fødselsdato = FaktumNavn(1, "Fødselsdato").faktum(LocalDate::class.java)
val ønsketDato = FaktumNavn(2, "Ønsker dagpenger fra dato").faktum(LocalDate::class.java)
val datoForBortfallPgaAlder = FaktumNavn(3, "Dato for bortfall på grunn av alder").faktum(LocalDate::class.java)
val VIRKNINGSTIDSPUNKT = FaktumNavn(4, "Virkningstidspunkt")
val sisteDagMedArbeidsplikt = FaktumNavn(5, "Siste dag med arbeidsplikt").faktum(LocalDate::class.java)
val registreringsdato = FaktumNavn(6, "Virkningstidspunkt").faktum(LocalDate::class.java)
val sisteDagMedLønn = FaktumNavn(7, "Siste dag med lønn").faktum(LocalDate::class.java)

val virkningstidspunkt =
    setOf(ønsketDato, sisteDagMedLønn, sisteDagMedArbeidsplikt, registreringsdato).faktum(VIRKNINGSTIDSPUNKT, MAKS_DATO)

val inntektSiste3år = FaktumNavn(8, "Inntekt siste 3 år").faktum(Inntekt::class.java)
val inntektSisteÅr = FaktumNavn(9, "Inntekt siste 12 mnd").faktum(Inntekt::class.java)
val dimisjonsdato = FaktumNavn(10, "Dimisjonsdato").faktum(LocalDate::class.java)
val inntekt3G = FaktumNavn(11, "3G").faktum(Inntekt::class.java)
val inntekt15G = FaktumNavn(12, "1,5G").faktum(Inntekt::class.java)

val egenBondegård = FaktumNavn(13, "Eier egen bondegård").faktum(Boolean::class.java)
val egenBedrift = FaktumNavn(14, "Eier egen bedrift").faktum(Boolean::class.java)
val fangstOgFisk = FaktumNavn(15, "Driver med fangst og fisk").faktum(Boolean::class.java)

val villigDeltid = FaktumNavn(16, "Villig til deltidsarbeid").faktum(Boolean::class.java)
val villigPendle = FaktumNavn(17, "Villig til pendling").faktum(Boolean::class.java)
val villigHelse = FaktumNavn(18, "Villig til helse").faktum(Boolean::class.java)
val villigJobb = FaktumNavn(19, "Villig til å ta ethvert arbeid").faktum(Boolean::class.java)
