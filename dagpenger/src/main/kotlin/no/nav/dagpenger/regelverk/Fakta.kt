package no.nav.dagpenger.regelverk

import no.nav.dagpenger.model.fakta.FaktumNavn
import no.nav.dagpenger.model.fakta.Inntekt
import java.time.LocalDate

val fødselsdatoNavn = FaktumNavn<LocalDate>(1, "Fødselsdato")
val ønsketDatoNavn = FaktumNavn<LocalDate>(2, "Ønsker dagpenger fra dato")
val datoForBortfallPgaAlderNavn = FaktumNavn<LocalDate>(3, "Dato for bortfall på grunn av alder")
val virkningstidspunktNavn = FaktumNavn<LocalDate>(4, "Virkningstidspunkt")

val sisteDagMedArbeidspliktNavn = FaktumNavn<LocalDate>(5, "Siste dag med arbeidsplikt")
val registreringsdatoNavn = FaktumNavn<LocalDate>(6, "Registreringsdato")
val sisteDagMedLønnNavn = FaktumNavn<LocalDate>(7, "Siste dag med lønn")

val inntektSiste3årNavn = FaktumNavn<Inntekt>(8, "Inntekt siste 3 år")
val inntektSisteÅrNavn = FaktumNavn<Inntekt>(9, "Inntekt siste 12 mnd")
val dimisjonsdatoNavn = FaktumNavn<LocalDate>(10, "Dimisjonsdato")
val inntekt3GNavn = FaktumNavn<Inntekt>(11, "3G")
val inntekt15GNavn = FaktumNavn<Inntekt>(12, "1,5G")

val egenBondegårdNavn = FaktumNavn<Boolean>(13, "Eier egen bondegård")
val egenBedriftNavn = FaktumNavn<Boolean>(14, "Eier egen bedrift")
val fangstOgFiskNavn = FaktumNavn<Boolean>(15, "Driver med fangst og fisk")

val villigDeltidNavn = FaktumNavn<Boolean>(16, "Villig til deltidsarbeid")
val villigPendleNavn = FaktumNavn<Boolean>(17, "Villig til pendling")
val villigHelseNavn = FaktumNavn<Boolean>(18, "Villig til helse")
val villigJobbNavn = FaktumNavn<Boolean>(19, "Villig til å ta ethvert arbeid")
