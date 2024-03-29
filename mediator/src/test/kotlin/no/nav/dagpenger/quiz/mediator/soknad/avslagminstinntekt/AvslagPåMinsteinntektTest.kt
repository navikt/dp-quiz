package no.nav.dagpenger.quiz.mediator.soknad.avslagminstinntekt

import no.nav.dagpenger.model.faktum.Dokument
import no.nav.dagpenger.model.faktum.Inntekt.Companion.årlig
import no.nav.dagpenger.model.helpers.desember
import no.nav.dagpenger.model.helpers.februar
import no.nav.dagpenger.model.helpers.januar
import no.nav.dagpenger.model.seksjon.Prosess
import no.nav.dagpenger.quiz.mediator.helpers.testPerson
import no.nav.dagpenger.quiz.mediator.soknad.Prosesser
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.antallEndredeArbeidsforhold
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.arenaFagsakId
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.behandlingsdato
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.eøsArbeid
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.grunnbeløp
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.harHattDagpengerSiste13mnd
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.harInntektNesteKalendermåned
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.hattLukkedeSakerSiste8Uker
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.helseTilAlleTyperJobb
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.innsendtSøknadsId
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.inntektSiste12mnd
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.inntektSiste36mnd
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.inntektsrapporteringsperiodeTom
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.jobbetUtenforNorge
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.kanJobbeDeltid
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.kanJobbeHvorSomHelst
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.lønnsgaranti
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.minsteinntektfaktor12mnd
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.minsteinntektfaktor36mnd
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.oppfyllerMinsteinntektManuell
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.ordinær
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.over67årFradato
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.permittert
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.permittertFiskeforedling
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.registrertArbeidssøkerPeriodeFom
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.registrertArbeidssøkerPeriodeTom
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.registrertArbeidssøkerPerioder
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.senesteMuligeVirkningsdato
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.sykepengerSiste36mnd
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.søknadstidspunkt
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.verneplikt
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.villigTilÅBytteYrke
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.ønsketDato
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.time.LocalDateTime

internal class AvslagPåMinsteinntektTest {
    private lateinit var manglerInntekt: Prosess

    @BeforeEach
    fun setup() {
        manglerInntekt =
            AvslagPåMinsteinntektOppsett.henvendelse.prosess(testPerson, Prosesser.AvslagPåMinsteinntekt).apply {
                dokument(arenaFagsakId).besvar(Dokument(LocalDateTime.now(), "urn:fagsakid:123123"))
                dokument(innsendtSøknadsId).besvar(Dokument(LocalDateTime.now(), "urn:soknadid:ABCD123"))

                dato(over67årFradato).besvar(1.desember)

                dato(behandlingsdato).besvar(5.januar)
                dato(ønsketDato).besvar(5.januar)
                dato(søknadstidspunkt).besvar(2.januar)
                dato(senesteMuligeVirkningsdato).besvar(19.januar)
                boolsk(harInntektNesteKalendermåned).besvar(false)

                boolsk(helseTilAlleTyperJobb).besvar(true)
                boolsk(kanJobbeHvorSomHelst).besvar(true)
                boolsk(villigTilÅBytteYrke).besvar(true)
                boolsk(kanJobbeDeltid).besvar(true)

                generator(registrertArbeidssøkerPerioder).besvar(1)
                dato("$registrertArbeidssøkerPeriodeFom.1").besvar(1.januar(2018))
                dato("$registrertArbeidssøkerPeriodeTom.1").besvar(30.januar(2018))

                boolsk(harHattDagpengerSiste13mnd).besvar(false)
                boolsk(hattLukkedeSakerSiste8Uker).besvar(false)
                boolsk(sykepengerSiste36mnd).besvar(false)

                dato(inntektsrapporteringsperiodeTom).besvar(5.februar)
                boolsk(eøsArbeid).besvar(false)
                boolsk(jobbetUtenforNorge).besvar(false)

                inntekt(grunnbeløp).besvar(100000.årlig)
                desimaltall(minsteinntektfaktor12mnd).besvar(1.5)
                desimaltall(minsteinntektfaktor36mnd).besvar(3.0)

                boolsk(verneplikt).besvar(false)

                inntekt(inntektSiste36mnd).besvar(20000.årlig)
                inntekt(inntektSiste12mnd).besvar(5000.årlig)

                generator(antallEndredeArbeidsforhold).besvar(1)
                boolsk("$ordinær.1").besvar(false)
                boolsk("$permittert.1").besvar(true)
                boolsk("$lønnsgaranti.1").besvar(false)
                boolsk("$permittertFiskeforedling.1").besvar(false)

                boolsk(oppfyllerMinsteinntektManuell).besvar(true) // Omgå manuell-seksjon
            }
    }

    @Test
    fun `De som er over 67 år får avslag`() {
        manglerInntekt.inntekt(inntektSiste36mnd).besvar(2000000.årlig)
        manglerInntekt.dato(over67årFradato).besvar(1.januar)

        assertTrue(manglerInntekt.erFerdig())
        assertEquals(false, manglerInntekt.resultat())
    }

    @Test
    fun `De som ikke oppfyller kravet til minsteinntekt får avslag`() {
        assertTrue(manglerInntekt.erFerdig())
        assertEquals(false, manglerInntekt.resultat())
    }

    @Test
    fun `Søknader fra brukere som har hatt dagpenger de siste 36 månedene blir ikke behandlet`() {
        manglerInntekt.boolsk(harHattDagpengerSiste13mnd).besvar(true)
        assertNesteSeksjon("mulig gjenopptak")
    }

    @Test
    fun `Søknader fra brukere som har hatt lukkede saker siste 8 uker blir ikke behandlet`() {
        manglerInntekt.boolsk(hattLukkedeSakerSiste8Uker).besvar(true)
        assertNesteSeksjon("har hatt lukkede saker siste 8 uker")
    }

    @Test
    fun `De som oppfyller kravet til minsteinntekt får innvilget dagpenger`() {
        manglerInntekt.inntekt(inntektSiste36mnd).besvar(2000000.årlig)
        manglerInntekt.inntekt(inntektSiste12mnd).besvar(5000000.årlig)
        assertEquals(true, manglerInntekt.resultat())
    }

    @Test
    fun `De som har avtjent verneplikt får innvilget dagpenger`() {
        manglerInntekt.boolsk(verneplikt).besvar(true)
        assertEquals(true, manglerInntekt.resultat())
    }

    @Test
    fun `Skal manuelt behandles når virkningsdato er senere enn dagens inntektsrapporteringsperiode`() {
        manglerInntekt.dato(inntektsrapporteringsperiodeTom).besvar(1.januar)
        assertNesteSeksjon("virkningstidspunkt vi ikke kan håndtere")
    }

    @Test
    fun `Skal manuelt behandles hvis har sykepenger`() {
        manglerInntekt.boolsk(sykepengerSiste36mnd).besvar(true)
        assertNesteSeksjon("svangerskapsrelaterte sykepenger")
    }

    @Test
    fun `Eøs arbeid skal manuelt behandles`() {
        manglerInntekt.boolsk(eøsArbeid).besvar(true)
        assertNesteSeksjon("EØS-arbeid")
    }

    @Test
    fun `Arbeidsforhold utenfor Norge skal manuelt behandles`() {
        manglerInntekt.boolsk(jobbetUtenforNorge).besvar(true)
        assertNesteSeksjon("jobbet utenfor Norge")
    }

    @Test
    fun `Inntekt neste kalendermåned skal manuelt behandles`() {
        manglerInntekt.boolsk(harInntektNesteKalendermåned).besvar(true)
        assertNesteSeksjon("det er inntekt neste kalendermåned")
    }

    @ParameterizedTest
    @ValueSource(ints = [kanJobbeDeltid, kanJobbeHvorSomHelst, helseTilAlleTyperJobb, villigTilÅBytteYrke])
    fun `Søkere som ikke er reelle arbeidssøkere skal manuelt behandles`(faktum: Int) {
        manglerInntekt.boolsk(faktum).besvar(false)
        assertNesteSeksjon("ikke reell arbeidssøker")
    }

    @Test
    fun `Ikke registrert arbeidssøker skal manuelt behandles`() {
        manglerInntekt.dato("$registrertArbeidssøkerPeriodeFom.1").besvar(1.januar(2017))
        manglerInntekt.dato("$registrertArbeidssøkerPeriodeTom.1").besvar(30.januar(2017))
        assertNesteSeksjon("ikke registrert arbeidssøker")
    }

    @Test
    fun `Aldri registrert arbeidssøker skal manuelt behandles`() {
        manglerInntekt.generator(registrertArbeidssøkerPerioder).besvar(0)
        assertNesteSeksjon("ikke registrert arbeidssøker")
    }

    private fun assertNesteSeksjon(navn: String) {
        assertFalse(manglerInntekt.nesteSeksjoner().isEmpty(), "Regeltre evaluert ferdig, ingen neste seksjon")
        assertEquals(navn, manglerInntekt.nesteSeksjoner().first().navn)
    }
}
