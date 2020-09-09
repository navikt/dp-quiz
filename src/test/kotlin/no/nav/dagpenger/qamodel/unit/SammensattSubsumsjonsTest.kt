package no.nav.dagpenger.qamodel.unit

import no.nav.dagpenger.qamodel.fakta.Faktum
import no.nav.dagpenger.qamodel.helpers.januar
import no.nav.dagpenger.qamodel.port.Inntekt
import no.nav.dagpenger.qamodel.subsumsjon.alle
import no.nav.dagpenger.qamodel.subsumsjon.før
import no.nav.dagpenger.qamodel.subsumsjon.ikkeFør
import no.nav.dagpenger.qamodel.subsumsjon.minstEnAv
import no.nav.dagpenger.qamodel.subsumsjon.så
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class SammensattSubsumsjonsTest {

    val bursdag67 = Faktum<LocalDate>("Datoen du fyller 67")
    val søknadsdato = Faktum<LocalDate>("Datoen du søker om dagpenger")
    val ønsketdato = Faktum<LocalDate>("Datoen du ønsker dagpenger fra")
    val sisteDagMedLønn = Faktum<LocalDate>("Siste dag du mottar lønn")
    val inntektSiste3år = Faktum<Inntekt>("Inntekt siste 36 måneder")
    val inntektSisteÅr = Faktum<Inntekt>("Inntekt siste 12 måneder")
    val verneplikt  = Faktum<LocalDate>("Dimisjonsdato")

    val comp = "inngangsvilkår".alle(
        "under67".alle(
            søknadsdato før bursdag67,
            ønsketdato før bursdag67,
            sisteDagMedLønn før bursdag67
        ),
        "kravdato er godkjent".alle(
            ønsketdato ikkeFør sisteDagMedLønn,
            søknadsdato ikkeFør sisteDagMedLønn,
        )
    ) så "oppfyller krav til minsteinntekt".minstEnAv(
            inntektSiste3år,
            inntektSisteÅr,
            verneplikt
    )

    @Test
    fun `subsumsjonen kan konkludere`() {
        println(comp)
        assertEquals(4, comp.fakta().size)
    }
}

