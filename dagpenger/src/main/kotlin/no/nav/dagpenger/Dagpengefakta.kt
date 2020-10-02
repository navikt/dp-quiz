package no.nav.dagpenger

import no.nav.dagpenger.model.fakta.Inntekt
import no.nav.dagpenger.model.fakta.faktum
import no.nav.dagpenger.model.regel.MAKS_DATO
import no.nav.dagpenger.regelverk.datoForBortfallPgaAlderNavn
import no.nav.dagpenger.regelverk.dimisjonsdatoNavn
import no.nav.dagpenger.regelverk.egenBedriftNavn
import no.nav.dagpenger.regelverk.egenBondegårdNavn
import no.nav.dagpenger.regelverk.fangstOgFiskNavn
import no.nav.dagpenger.regelverk.fødselsdatoNavn
import no.nav.dagpenger.regelverk.inntekt15GNavn
import no.nav.dagpenger.regelverk.inntekt3GNavn
import no.nav.dagpenger.regelverk.inntektSiste3årNavn
import no.nav.dagpenger.regelverk.inntektSisteÅrNavn
import no.nav.dagpenger.regelverk.registreringsdatoNavn
import no.nav.dagpenger.regelverk.sisteDagMedArbeidspliktNavn
import no.nav.dagpenger.regelverk.sisteDagMedLønnNavn
import no.nav.dagpenger.regelverk.villigDeltidNavn
import no.nav.dagpenger.regelverk.villigHelseNavn
import no.nav.dagpenger.regelverk.villigJobbNavn
import no.nav.dagpenger.regelverk.villigPendleNavn
import no.nav.dagpenger.regelverk.virkningstidspunktNavn
import no.nav.dagpenger.regelverk.ønsketDatoNavn
import java.time.LocalDate

class Dagpengefakta {
    val fødselsdato = fødselsdatoNavn.faktum(LocalDate::class.java)
    val villigDeltid = villigDeltidNavn.faktum(Boolean::class.java)
    val villigPendle = villigPendleNavn.faktum(Boolean::class.java)
    val villigHelse = villigHelseNavn.faktum(Boolean::class.java)
    val villigJobb = villigJobbNavn.faktum(Boolean::class.java)

    val ønsketDato = ønsketDatoNavn.faktum(LocalDate::class.java)
    val registreringsdato = registreringsdatoNavn.faktum(LocalDate::class.java)
    val sisteDagMedLønn = sisteDagMedLønnNavn.faktum(LocalDate::class.java)
    val sisteDagMedArbeidsplikt = sisteDagMedArbeidspliktNavn.faktum(LocalDate::class.java)

    val virkningstidspunkt =
        setOf(ønsketDato, registreringsdato, sisteDagMedArbeidsplikt, sisteDagMedLønn).faktum(
            virkningstidspunktNavn,
            MAKS_DATO
        )

    val datoForBortfallPgaAlder = datoForBortfallPgaAlderNavn.faktum(LocalDate::class.java)
    val dimisjonsdato = dimisjonsdatoNavn.faktum(LocalDate::class.java)
    val inntektSiste3År = inntektSiste3årNavn.faktum(Inntekt::class.java)
    val inntektSisteÅr = inntektSisteÅrNavn.faktum(Inntekt::class.java)
    val inntekt3G = inntekt3GNavn.faktum(Inntekt::class.java)
    val inntekt15G = inntekt15GNavn.faktum(Inntekt::class.java)

    val egenBondegård = egenBondegårdNavn.faktum(Boolean::class.java)
    val egenBedrift = egenBedriftNavn.faktum(Boolean::class.java)
    val fangstOgFisk = fangstOgFiskNavn.faktum(Boolean::class.java)
}
