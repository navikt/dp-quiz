package no.nav.dagpenger.qamodel.unit

import no.nav.dagpenger.qamodel.fakta.Faktum
import no.nav.dagpenger.qamodel.helpers.januar
import no.nav.dagpenger.qamodel.port.Inntekt
import no.nav.dagpenger.qamodel.port.Inntekt.Companion.månedlig
import no.nav.dagpenger.qamodel.subsumsjon.alle
import no.nav.dagpenger.qamodel.subsumsjon.eller
import no.nav.dagpenger.qamodel.subsumsjon.etter
import no.nav.dagpenger.qamodel.subsumsjon.før
import no.nav.dagpenger.qamodel.subsumsjon.ikkeFør
import no.nav.dagpenger.qamodel.subsumsjon.minst
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
    val dimisjonsdato = Faktum<LocalDate>("Dimisjonsdato")

    val virkningstidspunkt = Faktum<LocalDate>("Hvilken dato vedtaket skal gjelde fra")

    val inntekt3G = Faktum<Inntekt>("3G")
    val inntekt15G = Faktum<Inntekt>("1.5G")

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
    ) så (
        "oppfyller krav til minsteinntekt".minstEnAv(
            inntektSiste3år minst inntekt3G,
            inntektSisteÅr minst inntekt15G,
            dimisjonsdato etter virkningstidspunkt
        ) eller "oppfyller ikke kravet til minsteinntekt".alle(
            ønsketdato ikkeFør sisteDagMedLønn
        )
    ) eller "oppfyller ikke inngangsvilkår".alle(
        ønsketdato ikkeFør sisteDagMedLønn
    )

    @Test
    fun `kan besøke alle fakta fra treet`() {
        println(comp)
        assertEquals(10, comp.fakta().size)
    }

    @Test
    fun `hvilke fakta behøves nå`() {
        assertEquals(4, comp.nesteFakta().size)
    }

    @Test
    fun `test`() {
        assertEquals(10, comp.fakta().size)
        ønsketdato.besvar(2.januar)
        søknadsdato.besvar(2.januar)
        sisteDagMedLønn.besvar(1.januar)
        assertEquals(1, comp.nesteFakta().size)
        bursdag67.besvar(31.januar)
        assertEquals(6, comp.nesteFakta().size)
        inntektSisteÅr.besvar(100000.månedlig)
        dimisjonsdato.besvar(1.januar)
        assertEquals(4, comp.nesteFakta().size)
        assertEquals(10, comp.fakta().size)
        println(comp)
    }
}
