package no.nav.dagpenger.quiz.mediator.soknad

import no.nav.dagpenger.model.faktum.Dokument
import no.nav.dagpenger.model.faktum.Identer
import no.nav.dagpenger.model.faktum.Inntekt.Companion.årlig
import no.nav.dagpenger.model.faktum.Person
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.model.seksjon.Versjon
import no.nav.dagpenger.quiz.mediator.helpers.desember
import no.nav.dagpenger.quiz.mediator.helpers.februar
import no.nav.dagpenger.quiz.mediator.helpers.januar
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntekt.regeltre
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.antallEndredeArbeidsforhold
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.arenaFagsakId
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.behandlingsdato
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.eøsArbeid
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.fangstOgFisk
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.fortsattRettKorona
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.grunnbeløp
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.harHattDagpengerSiste36mnd
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.harInntektNesteKalendermåned
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.helseTilAlleTyperJobb
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.innsendtSøknadsId
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.inntektSiste12mnd
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.inntektSiste36mnd
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.inntektsrapporteringsperiodeTom
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.kanJobbeDeltid
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.kanJobbeHvorSomHelst
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.lærling
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.lønnsgaranti
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.minsteinntektfaktor12mnd
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.minsteinntektfaktor36mnd
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.oppfyllerMinsteinntektManuell
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.ordinær
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.over67årFradato
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.permittert
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.permittertFiskeforedling
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.registrertArbeidssøkerPeriodeFom
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.registrertArbeidssøkerPeriodeTom
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.registrertArbeidssøkerPerioder
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.senesteMuligeVirkningsdato
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.sykepengerSiste36mnd
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.søknad
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.søknadstidspunkt
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.verneplikt
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.villigTilÅBytteYrke
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett.ønsketDato
import no.nav.dagpenger.quiz.mediator.soknad.Seksjoner.søknadprosess
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.time.LocalDateTime
import java.util.UUID

internal class RegeltreTest {
    private lateinit var manglerInntekt: Søknadprosess

    @BeforeEach
    fun setup() {
        manglerInntekt = Versjon.Bygger(søknad, regeltre, mapOf(Versjon.UserInterfaceType.Web to søknadprosess))
            .søknadprosess(
                Person(UUID.randomUUID(), Identer.Builder().folkeregisterIdent("12345678910").build()),
                Versjon.UserInterfaceType.Web
            )

        manglerInntekt.apply {
            dokument(arenaFagsakId).besvar(Dokument(LocalDateTime.now(), "123123"))
            dokument(innsendtSøknadsId).besvar(Dokument(LocalDateTime.now(), "ABCD123"))

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

            boolsk(harHattDagpengerSiste36mnd).besvar(false)
            boolsk(sykepengerSiste36mnd).besvar(false)

            dato(inntektsrapporteringsperiodeTom).besvar(5.februar)
            boolsk(eøsArbeid).besvar(false)

            boolsk(fangstOgFisk).besvar(false)

            inntekt(grunnbeløp).besvar(100000.årlig)
            desimaltall(minsteinntektfaktor12mnd).besvar(1.5)
            desimaltall(minsteinntektfaktor36mnd).besvar(3.0)

            boolsk(verneplikt).besvar(false)
            boolsk(lærling).besvar(false)

            inntekt(inntektSiste36mnd).besvar(20000.årlig)
            inntekt(inntektSiste12mnd).besvar(5000.årlig)

            generator(antallEndredeArbeidsforhold).besvar(1)
            boolsk("$ordinær.1").besvar(false)
            boolsk("$permittert.1").besvar(true)
            boolsk("$lønnsgaranti.1").besvar(false)
            boolsk("$permittertFiskeforedling.1").besvar(false)

            boolsk(fortsattRettKorona).besvar(false)

            boolsk(oppfyllerMinsteinntektManuell).besvar(true) // Omgå manuell-seksjon
        }
    }

    @Test
    @Disabled
    fun `De som er over 67 år får avslag`() {
        manglerInntekt.inntekt(inntektSiste36mnd).besvar(2000000.årlig)
        manglerInntekt.dato(over67årFradato).besvar(1.januar)

        assertTrue(manglerInntekt.erFerdig())
        assertEquals(false, manglerInntekt.resultat())
    }

    @Test
    fun `Skal manuelt behandles hvis over 67`() {
        manglerInntekt.inntekt(inntektSiste36mnd).besvar(2000000.årlig)
        manglerInntekt.dato(over67årFradato).besvar(1.januar)

        assertNesteSeksjon("over 67 år")
    }

    @Test
    fun `De som ikke oppfyller kravet til minsteinntekt får avslag`() {
        assertTrue(manglerInntekt.erFerdig())
        assertEquals(false, manglerInntekt.resultat())
    }

    @Test
    fun `Søknader fra brukere som har hatt dagpenger de siste 36 månedene blir ikke behandlet`() {
        manglerInntekt.boolsk(harHattDagpengerSiste36mnd).besvar(true)
        assertNesteSeksjon("mulig gjenopptak")
    }

    @Test
    fun `De som oppfyller kravet til minsteinntekt får innvilget dagpenger`() {
        manglerInntekt.inntekt(inntektSiste36mnd).besvar(2000000.årlig)
        manglerInntekt.inntekt(inntektSiste12mnd).besvar(5000000.årlig)
        assertEquals(true, manglerInntekt.resultat())
    }

    @Test
    fun `De som har vært lærling får innvilget dagpenger`() {
        manglerInntekt.boolsk(lærling).besvar(true)
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
    fun `Fangst og fisk skal manuelt behandles`() {
        manglerInntekt.boolsk(fangstOgFisk).besvar(true)
        assertNesteSeksjon("mulige inntekter fra fangst og fisk")
    }

    @Test
    fun `Eøs arbeid skal manuelt behandles`() {
        manglerInntekt.boolsk(eøsArbeid).besvar(true)
        assertNesteSeksjon("EØS-arbeid")
    }

    @Test
    fun `Inntekt neste kalendermåned skal manuelt behandles`() {
        manglerInntekt.boolsk(harInntektNesteKalendermåned).besvar(true)
        assertNesteSeksjon("det er inntekt neste kalendermåned")
    }

    @Test
    fun `Flere arbeidsforhold skal manuelt behandles`() {
        manglerInntekt.heltall(antallEndredeArbeidsforhold).besvar(2)
        assertNesteSeksjon("flere arbeidsforhold")
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

    @Test
    fun `Har fortsatt rett til dagpenger under korona skal manuelt behandles`() {
        manglerInntekt.boolsk(fortsattRettKorona).besvar(true)
        assertNesteSeksjon("fortsatt rett korona")
    }

    private fun assertNesteSeksjon(navn: String) {
        assertFalse(manglerInntekt.nesteSeksjoner().isEmpty(), "Regeltre evaluert ferdig, ingen neste seksjon")
        assertEquals(navn, manglerInntekt.nesteSeksjoner().first().navn)
    }
}
