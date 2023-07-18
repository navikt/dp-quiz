package no.nav.dagpenger.quiz.mediator.soknad.dagpenger

import no.nav.dagpenger.model.faktum.Fakta
import no.nav.dagpenger.model.faktum.Faktum
import no.nav.dagpenger.model.faktum.Flervalg
import no.nav.dagpenger.model.faktum.Land
import no.nav.dagpenger.model.faktum.Periode
import no.nav.dagpenger.model.faktum.Tekst
import no.nav.dagpenger.model.helpers.MedSøknad
import no.nav.dagpenger.model.seksjon.Prosess
import no.nav.dagpenger.quiz.mediator.helpers.testFaktaversjon
import no.nav.dagpenger.quiz.mediator.helpers.testSøknadprosess
import no.nav.dagpenger.quiz.mediator.land.Landfabrikken.eøsEllerSveits
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.AndreYtelser.`andre ytelser mottatt eller søkt`
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.AndreYtelser.`annen ytelse hvem utebetaler`
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.AndreYtelser.`annen ytelse hvilken periode`
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.AndreYtelser.`arbeidsløs GFF hvilken periode`
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.AndreYtelser.`dagpenger eøs land hvilken periode`
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.AndreYtelser.`dagpenger hvilket eøs land utbetaler`
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.AndreYtelser.`etterlønn arbeidsgiver hvem utbetaler`
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.AndreYtelser.`etterlønn arbeidsgiver hvilken periode`
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.AndreYtelser.`garantilott fra GFF hvilken periode`
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.AndreYtelser.`hvilke andre ytelser`
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.AndreYtelser.`hvilken annen ytelse`
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.AndreYtelser.`tjenestepensjon hvem utbetaler`
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.AndreYtelser.`tjenestepensjon hvilken periode`
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.AndreYtelser.`utbetaling eller økonomisk gode tidligere arbeidsgiver`
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.AndreYtelser.`økonomisk gode tidligere arbeidsgiver hva omfatter avtalen`
import no.nav.dagpenger.quiz.mediator.soknad.verifiserFeltsammensetting
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate
import kotlin.test.assertFalse

internal class AndreYtelserTest {
    @Test
    fun `Sjekk om faktasammensettingen har endret seg siden sist`() {
        AndreYtelser.verifiserFeltsammensetting(29, 145435)
    }

    @Test
    fun `Hvis bruker ikke får noen andre ytelser`() {
        val fakta = Fakta(testFaktaversjon(), *AndreYtelser.fakta())
        val søknadprosess = fakta.testSøknadprosess(
            subsumsjon = AndreYtelser.regeltre(fakta),
        ) {
            AndreYtelser.seksjon(this)
        }

        søknadprosess.boolsk(`andre ytelser mottatt eller søkt`).besvar(false)
        søknadprosess.boolsk(`utbetaling eller økonomisk gode tidligere arbeidsgiver`).besvar(false)
        assertEquals(true, søknadprosess.resultat())

        søknadprosess.boolsk(`utbetaling eller økonomisk gode tidligere arbeidsgiver`).besvar(true)
        assertEquals(null, søknadprosess.resultat())
        søknadprosess.tekst(`økonomisk gode tidligere arbeidsgiver hva omfatter avtalen`)
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
            søknadprosess.boolsk(`utbetaling eller økonomisk gode tidligere arbeidsgiver`).besvar(true)
            søknadprosess.tekst(`økonomisk gode tidligere arbeidsgiver hva omfatter avtalen`)
                .besvar(Tekst("dummy begrunnelse"))

            assertEquals(true, søknadprosess.resultat())
        }
    }

    @Test
    fun Avhengigheter() {
        verifiserAnnenYtelseUtenØkonomiskGode { søknadprosess ->
            besvarAlleFaktaForTjenestepensjon(søknadprosess)
            besvarAlleFaktaForGFF(søknadprosess)
            besvarAlleFaktaForGarantiloggFraGFF(søknadprosess)
            besvarAlleFaktaForEtterlønn(søknadprosess)
            besvarAlleFaktaForDagpengerFraAnnetEØSLand(søknadprosess)
            besvarAlleFaktaForAnnenYtelse(søknadprosess)
            søknadprosess.boolsk(`utbetaling eller økonomisk gode tidligere arbeidsgiver`).besvar(true)
            søknadprosess.tekst(`økonomisk gode tidligere arbeidsgiver hva omfatter avtalen`)
                .besvar(Tekst("dummy begrunnelse"))

            søknadprosess.boolsk(`andre ytelser mottatt eller søkt`).besvar(false)

            assertErUbesvarte(
                søknadprosess.flervalg(`hvilke andre ytelser`),
                søknadprosess.tekst(`tjenestepensjon hvem utbetaler`),
                søknadprosess.periode(`tjenestepensjon hvilken periode`),
                søknadprosess.periode(`arbeidsløs GFF hvilken periode`),
                søknadprosess.periode(`garantilott fra GFF hvilken periode`),
                søknadprosess.tekst(`etterlønn arbeidsgiver hvem utbetaler`),
                søknadprosess.periode(`etterlønn arbeidsgiver hvilken periode`),
                søknadprosess.land(`dagpenger hvilket eøs land utbetaler`),
                søknadprosess.periode(`dagpenger eøs land hvilken periode`),
                søknadprosess.tekst(`hvilken annen ytelse`),
                søknadprosess.tekst(`annen ytelse hvem utebetaler`),
                søknadprosess.periode(`annen ytelse hvilken periode`),
            )
        }
    }

    @Test
    fun `Faktarekkefølge i seksjon`() {
        val fakta = Fakta(testFaktaversjon(), *AndreYtelser.fakta())
        val søknadprosess = fakta.testSøknadprosess(
            subsumsjon = AndreYtelser.regeltre(fakta),
        ) {
            AndreYtelser.seksjon(this)
        }
        val faktarekkefølge = søknadprosess.nesteSeksjoner().first().joinToString(separator = ",") { it.id }
        assertEquals(
            "5001,5002,5003,5004,5014,5015,5005,5006,5007,5008,5009,5010,5011,5012,5013,5016,5017,5018,5019,5020,5021,5022,5023,5024,5025,5026,5027,5028,5029",
            faktarekkefølge,
        )
    }

    @Test
    fun `For et EØS-land skal det være en egen gruppe for kun EØS-land`() {
        val fakta = Fakta(testFaktaversjon(), *AndreYtelser.fakta())
        val søknadprosess = fakta.testSøknadprosess(
            subsumsjon = AndreYtelser.regeltre(fakta),
        ) {
            AndreYtelser.seksjon(this)
        }
        besvarAlleFaktaForDagpengerFraAnnetEØSLand(søknadprosess)

        MedSøknad(søknadprosess) {
            harAntallSeksjoner(1)
            seksjon("andre-ytelser") {
                fakta(sjekkAlle = false, sjekkRekkefølge = false) {
                    land("faktum.dagpenger-hvilket-eos-land-utbetaler") {
                        grupper(sjekkAlle = false) {
                            gruppe("faktum.dagpenger-hvilket-eos-land-utbetaler.gruppe.eøs") {
                                eøsEllerSveits.forEach { eøsLand ->
                                    harLand(eøsLand.alpha3Code)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun verifiserAnnenYtelseUtenØkonomiskGode(kodeForSpesifikkYtelse: (Prosess) -> Unit) {
        val fakta = Fakta(testFaktaversjon(), *AndreYtelser.fakta())
        val søknadprosess = fakta.testSøknadprosess(
            subsumsjon = AndreYtelser.regeltre(fakta),
        ) {
            AndreYtelser.seksjon(this)
        }
        søknadprosess.boolsk(`andre ytelser mottatt eller søkt`).besvar(true)
        søknadprosess.boolsk(`utbetaling eller økonomisk gode tidligere arbeidsgiver`).besvar(false)

        kodeForSpesifikkYtelse(søknadprosess)

        assertEquals(true, søknadprosess.resultat())
    }

    private fun besvarAlleFaktaForTjenestepensjon(prosess: Prosess) {
        prosess.flervalg(`hvilke andre ytelser`)
            .besvar(Flervalg("faktum.hvilke-andre-ytelser.svar.pensjon-offentlig-tjenestepensjon"))
        prosess.tekst(`tjenestepensjon hvem utbetaler`).besvar(Tekst("dummy arbeidsgiver"))
        val nå = LocalDate.now()
        prosess.periode(`tjenestepensjon hvilken periode`).besvar(Periode(nå.minusYears(2), nå))
    }

    private fun besvarAlleFaktaForGFF(prosess: Prosess) {
        prosess.flervalg(`hvilke andre ytelser`)
            .besvar(Flervalg("faktum.hvilke-andre-ytelser.svar.arbeidsloshet-garantikassen-for-fiskere"))
        val nå = LocalDate.now()
        prosess.periode(`arbeidsløs GFF hvilken periode`).besvar(Periode(nå.minusYears(3), nå))
    }

    private fun besvarAlleFaktaForGarantiloggFraGFF(prosess: Prosess) {
        prosess.flervalg(`hvilke andre ytelser`)
            .besvar(Flervalg("faktum.hvilke-andre-ytelser.svar.garantilott-garantikassen-for-fiskere"))
        val nå = LocalDate.now()
        prosess.periode(`garantilott fra GFF hvilken periode`).besvar(Periode(nå.minusYears(4), nå))
    }

    private fun besvarAlleFaktaForEtterlønn(prosess: Prosess) {
        prosess.flervalg(`hvilke andre ytelser`)
            .besvar(Flervalg("faktum.hvilke-andre-ytelser.svar.etterlonn-arbeidsgiver"))
        prosess.tekst(`etterlønn arbeidsgiver hvem utbetaler`).besvar(Tekst("dummy arbeidsgiver"))
        val nå = LocalDate.now()
        prosess.periode(`etterlønn arbeidsgiver hvilken periode`).besvar(Periode(nå.minusYears(5), nå))
    }

    private fun besvarAlleFaktaForDagpengerFraAnnetEØSLand(prosess: Prosess) {
        prosess.flervalg(`hvilke andre ytelser`)
            .besvar(Flervalg("faktum.hvilke-andre-ytelser.svar.dagpenger-annet-eos-land"))
        prosess.land(`dagpenger hvilket eøs land utbetaler`).besvar(Land("SWE"))
        val nå = LocalDate.now()
        prosess.periode(`dagpenger eøs land hvilken periode`).besvar(Periode(nå.minusYears(6), nå))
    }

    private fun besvarAlleFaktaForAnnenYtelse(prosess: Prosess) {
        prosess.flervalg(`hvilke andre ytelser`).besvar(Flervalg("faktum.hvilke-andre-ytelser.svar.annen-ytelse"))
        prosess.tekst(`hvilken annen ytelse`).besvar(Tekst("Annen dummy ytelse"))
        prosess.tekst(`annen ytelse hvem utebetaler`).besvar(Tekst("Dummy utbetaler"))
        val nå = LocalDate.now()
        prosess.periode(`annen ytelse hvilken periode`).besvar(Periode(nå.minusYears(7), nå))
    }

    private fun assertErUbesvarte(vararg fakta: Faktum<*>) =
        fakta.forEach { faktum ->
            assertFalse(faktum.erBesvart())
        }
}
