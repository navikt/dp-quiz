package no.nav.dagpenger.quiz.mediator.soknad.dagpenger

import no.nav.dagpenger.model.faktum.Faktum
import no.nav.dagpenger.model.faktum.Flervalg
import no.nav.dagpenger.model.faktum.Land
import no.nav.dagpenger.model.faktum.Periode
import no.nav.dagpenger.model.faktum.Prosessversjon
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.faktum.Tekst
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.quiz.mediator.helpers.testSøknadprosess
import no.nav.dagpenger.quiz.mediator.soknad.Prosess
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.AndreYtelser.`andre ytelser mottatt eller sokt`
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.AndreYtelser.`annen ytelse hvem utebetaler`
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.AndreYtelser.`annen ytelse hvilken periode`
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.AndreYtelser.`arbeidsløs GFF hvilken periode`
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.AndreYtelser.`dagpenger eos land hvilken periode`
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.AndreYtelser.`dagpenger hvilket eos land utbetaler`
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.AndreYtelser.`etterlonn arbeidsgiver hvem utbetaler`
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.AndreYtelser.`etterlonn arbeidsgiver hvilken periode`
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.AndreYtelser.`garantilott fra GFF hvilken periode`
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.AndreYtelser.`hvilke andre ytelser`
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.AndreYtelser.`hvilken annen ytelse`
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.AndreYtelser.`okonomisk gode tidligere arbeidsgiver hva omfatter avtalen`
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.AndreYtelser.`tjenestepensjon hvem utbetaler`
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.AndreYtelser.`tjenestepensjon hvilken periode`
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.AndreYtelser.`utbetaling eller okonomisk gode tidligere arbeidsgiver`
import no.nav.dagpenger.quiz.mediator.soknad.verifiserFeltsammensetting
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class AndreYtelserTest {
    @Test
    fun `Sjekk om faktasammensettingen har endret seg siden sist`() {
        AndreYtelser.verifiserFeltsammensetting(15, 75120)
    }

    @Test
    fun `Hvis bruker ikke får noen andre ytelser`() {
        val søknad = Søknad(Prosessversjon(Prosess.Dagpenger, -1), *AndreYtelser.fakta())
        val søknadprosess = søknad.testSøknadprosess(
            AndreYtelser.regeltre(søknad)
        ) {
            AndreYtelser.seksjon(this)
        }

        søknadprosess.boolsk(`andre ytelser mottatt eller sokt`).besvar(false)
        søknadprosess.boolsk(`utbetaling eller okonomisk gode tidligere arbeidsgiver`).besvar(false)
        assertEquals(true, søknadprosess.resultat())

        søknadprosess.boolsk(`utbetaling eller okonomisk gode tidligere arbeidsgiver`).besvar(true)
        assertEquals(null, søknadprosess.resultat())
        søknadprosess.tekst(`okonomisk gode tidligere arbeidsgiver hva omfatter avtalen`)
            .besvar(Tekst("dummy begrunnelse"))
        assertEquals(true, søknadprosess.resultat())
    }

    @Test
    fun `Bruker får kun tjenestepensjon`() {
        verifiserAnnenYtelseUtenØkonomiskGode { søknadprosess ->
            besvarAlleFaktaForTjenestepensjon(søknadprosess)
        }
    }

    @Test
    fun `Bruker får kun fra GFF (Garantikassen For Fiskere)`() {
        verifiserAnnenYtelseUtenØkonomiskGode { søknadprosess ->
            besvarAlleFaktaForGFF(søknadprosess)
        }
    }

    @Test
    fun `Bruker får kun garantilogg fra GFF (Garantikassen For Fiskere)`() {
        verifiserAnnenYtelseUtenØkonomiskGode { søknadprosess ->
            besvarAlleFaktaForGarantiloggFraGFF(søknadprosess)
        }
    }

    @Test
    fun `Bruker får kun etterlønn fra arbeidsgiver`() {
        verifiserAnnenYtelseUtenØkonomiskGode { søknadprosess ->
            besvarAlleFaktaForEtterlønn(søknadprosess)
        }
    }

    @Test
    fun `Bruker får kun dagpenger fra annet EØS-land`() {
        verifiserAnnenYtelseUtenØkonomiskGode { søknadprosess ->
            besvarAlleFaktaForDagpengerFraAnnetEØSLand(søknadprosess)
        }
    }

    @Test
    fun `Bruker får kun en annen ytelse`() {
        verifiserAnnenYtelseUtenØkonomiskGode { søknadprosess ->
            besvarAlleFaktaForAnnenYtelse(søknadprosess)
        }
    }

    @Test
    fun `Bruker har valgt alle mulige ytelser, og besvart alle fakta`() {
        verifiserAnnenYtelseUtenØkonomiskGode { søknadprosess ->
            besvarAlleFaktaForTjenestepensjon(søknadprosess)
            besvarAlleFaktaForGFF(søknadprosess)
            besvarAlleFaktaForGarantiloggFraGFF(søknadprosess)
            besvarAlleFaktaForEtterlønn(søknadprosess)
            besvarAlleFaktaForDagpengerFraAnnetEØSLand(søknadprosess)
            besvarAlleFaktaForAnnenYtelse(søknadprosess)
            søknadprosess.boolsk(`utbetaling eller okonomisk gode tidligere arbeidsgiver`).besvar(true)
            søknadprosess.tekst(`okonomisk gode tidligere arbeidsgiver hva omfatter avtalen`)
                .besvar(Tekst("dummy begrunnelse"))

            søknadprosess.forEach { seksjon ->
                seksjon.forEach { faktum ->
                    assertTrue(faktum.erBesvart(), "Faktum med id=${faktum.id} skal være besvart")
                }
            }
        }
    }

    @Test
    fun `Avhengigheter`() {
        verifiserAnnenYtelseUtenØkonomiskGode { søknadprosess ->
            besvarAlleFaktaForTjenestepensjon(søknadprosess)
            besvarAlleFaktaForGFF(søknadprosess)
            besvarAlleFaktaForGarantiloggFraGFF(søknadprosess)
            besvarAlleFaktaForEtterlønn(søknadprosess)
            besvarAlleFaktaForDagpengerFraAnnetEØSLand(søknadprosess)
            besvarAlleFaktaForAnnenYtelse(søknadprosess)
            søknadprosess.boolsk(`utbetaling eller okonomisk gode tidligere arbeidsgiver`).besvar(true)
            søknadprosess.tekst(`okonomisk gode tidligere arbeidsgiver hva omfatter avtalen`)
                .besvar(Tekst("dummy begrunnelse"))

            søknadprosess.forEach { seksjon ->
                seksjon.forEach { faktum ->
                    assertTrue(faktum.erBesvart(), "Faktum med id=${faktum.id} skal være besvart")
                }
            }

            søknadprosess.boolsk(`andre ytelser mottatt eller sokt`).besvar(false)

            assertErUbesvarte(
                søknadprosess.flervalg(`hvilke andre ytelser`),
                søknadprosess.tekst(`tjenestepensjon hvem utbetaler`),
                søknadprosess.periode(`tjenestepensjon hvilken periode`),
                søknadprosess.periode(`arbeidsløs GFF hvilken periode`),
                søknadprosess.periode(`garantilott fra GFF hvilken periode`),
                søknadprosess.tekst(`etterlonn arbeidsgiver hvem utbetaler`),
                søknadprosess.periode(`etterlonn arbeidsgiver hvilken periode`),
                søknadprosess.land(`dagpenger hvilket eos land utbetaler`),
                søknadprosess.periode(`dagpenger eos land hvilken periode`),
                søknadprosess.tekst(`hvilken annen ytelse`),
                søknadprosess.tekst(`annen ytelse hvem utebetaler`),
                søknadprosess.periode(`annen ytelse hvilken periode`)
            )
        }
    }

    private fun verifiserAnnenYtelseUtenØkonomiskGode(kodeForSpesifikkYtelse: (Søknadprosess) -> Unit) {
        val søknad = Søknad(Prosessversjon(Prosess.Dagpenger, -1), *AndreYtelser.fakta())
        val søknadprosess = søknad.testSøknadprosess(
            AndreYtelser.regeltre(søknad)
        ) {
            AndreYtelser.seksjon(this)
        }
        søknadprosess.boolsk(`andre ytelser mottatt eller sokt`).besvar(true)
        søknadprosess.boolsk(`utbetaling eller okonomisk gode tidligere arbeidsgiver`).besvar(false)

        kodeForSpesifikkYtelse(søknadprosess)

        assertEquals(true, søknadprosess.resultat())
    }

    private fun besvarAlleFaktaForTjenestepensjon(søknadprosess: Søknadprosess) {
        søknadprosess.flervalg(`hvilke andre ytelser`)
            .besvar(Flervalg("faktum.hvilke-andre-ytelser.svar.pensjon-offentlig-tjenestepensjon"))
        søknadprosess.tekst(`tjenestepensjon hvem utbetaler`).besvar(Tekst("dummy arbeidsgiver"))
        val nå = LocalDate.now()
        søknadprosess.periode(`tjenestepensjon hvilken periode`).besvar(Periode(nå.minusYears(2), nå))
    }

    private fun besvarAlleFaktaForGFF(søknadprosess: Søknadprosess) {
        søknadprosess.flervalg(`hvilke andre ytelser`)
            .besvar(Flervalg("faktum.hvilke-andre-ytelser.svar.arbeidsloshet-garantikassen-for-fiskere"))
        val nå = LocalDate.now()
        søknadprosess.periode(`arbeidsløs GFF hvilken periode`).besvar(Periode(nå.minusYears(3), nå))
    }

    private fun besvarAlleFaktaForGarantiloggFraGFF(søknadprosess: Søknadprosess) {
        søknadprosess.flervalg(`hvilke andre ytelser`)
            .besvar(Flervalg("faktum.hvilke-andre-ytelser.svar.garantilott-garantikassen-for-fiskere"))
        val nå = LocalDate.now()
        søknadprosess.periode(`garantilott fra GFF hvilken periode`).besvar(Periode(nå.minusYears(4), nå))
    }

    private fun besvarAlleFaktaForEtterlønn(søknadprosess: Søknadprosess) {
        søknadprosess.flervalg(`hvilke andre ytelser`)
            .besvar(Flervalg("faktum.hvilke-andre-ytelser.svar.etterlonn-arbeidsgiver"))
        søknadprosess.tekst(`etterlonn arbeidsgiver hvem utbetaler`).besvar(Tekst("dummy arbeidsgiver"))
        val nå = LocalDate.now()
        søknadprosess.periode(`etterlonn arbeidsgiver hvilken periode`).besvar(Periode(nå.minusYears(5), nå))
    }

    private fun besvarAlleFaktaForDagpengerFraAnnetEØSLand(søknadprosess: Søknadprosess) {
        søknadprosess.flervalg(`hvilke andre ytelser`)
            .besvar(Flervalg("faktum.hvilke-andre-ytelser.svar.dagpenger-annet-eos-land"))
        søknadprosess.land(`dagpenger hvilket eos land utbetaler`).besvar(Land("SWE"))
        val nå = LocalDate.now()
        søknadprosess.periode(`dagpenger eos land hvilken periode`).besvar(Periode(nå.minusYears(6), nå))
    }

    private fun besvarAlleFaktaForAnnenYtelse(søknadprosess: Søknadprosess) {
        søknadprosess.flervalg(`hvilke andre ytelser`).besvar(Flervalg("faktum.hvilke-andre-ytelser.svar.annen-ytelse"))
        søknadprosess.tekst(`hvilken annen ytelse`).besvar(Tekst("Annen dummy ytelse"))
        søknadprosess.tekst(`annen ytelse hvem utebetaler`).besvar(Tekst("Dummy utbetaler"))
        val nå = LocalDate.now()
        søknadprosess.periode(`annen ytelse hvilken periode`).besvar(Periode(nå.minusYears(7), nå))
    }

    private fun assertErUbesvarte(vararg fakta: Faktum<*>) =
        fakta.forEach { faktum ->
            assertFalse(faktum.erBesvart())
        }
}
