package no.nav.dagpenger.quiz.mediator.soknad.dagpenger

import no.nav.dagpenger.model.faktum.Dokument
import no.nav.dagpenger.model.faktum.Prosessversjon
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.faktum.Tekst
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.quiz.mediator.helpers.testSøknadprosess
import no.nav.dagpenger.quiz.mediator.soknad.Prosess
import no.nav.dagpenger.quiz.mediator.soknad.verifiserFeltsammensetting
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class DokumentasjonskravEgenNæringTest {
    private val dokumentfakta = EgenNæring.fakta() + DokumentasjonskravEgenNæring.fakta()
    private val søknad = Søknad(Prosessversjon(Prosess.Dagpenger, -1), *dokumentfakta)
    private lateinit var søknadprosess: Søknadprosess

    @Test
    fun `Sjekk om faktasammensettingen har endret seg siden sist`() {
        DokumentasjonskravEgenNæring.verifiserFeltsammensetting(6, 78021)
    }

    @BeforeEach
    fun setup() {
        søknadprosess = søknad.testSøknadprosess(
            DokumentasjonskravEgenNæring.regeltre(søknad)
        )
    }

    @Test
    fun `Verken egen næring eller gårdsbruk`() {
        søknadprosess.boolsk(EgenNæring.`driver du egen naering`).besvar(false)
        søknadprosess.boolsk(EgenNæring.`driver du eget gaardsbruk`).besvar(false)
        assertEquals(true, søknadprosess.resultat())
    }

    @Test
    fun `Driver egen næring og har dokumentasjon på arbeidstimer`() {
        søknadprosess.boolsk(EgenNæring.`driver du egen naering`).besvar(true)
        søknadprosess.boolsk(DokumentasjonskravEgenNæring.`dokumentasjon på arbeidstimer for egen næring tilgjengelig`).besvar(true)
        søknadprosess.dokument(DokumentasjonskravEgenNæring.`dokumentasjon på arbeidstimer for egen næring`).besvar(Dokument(lastOppTidsstempel = LocalDate.now(), "urn:nav:vedlegg:1"))
        søknadprosess.boolsk(EgenNæring.`driver du eget gaardsbruk`).besvar(false)
        assertEquals(true, søknadprosess.resultat())
    }

    @Test
    fun `Driver egen næring men har ikke dokumentasjon på arbeidstimer`() {
        søknadprosess.boolsk(EgenNæring.`driver du egen naering`).besvar(true)
        søknadprosess.boolsk(DokumentasjonskravEgenNæring.`dokumentasjon på arbeidstimer for egen næring tilgjengelig`).besvar(false)
        søknadprosess.tekst(DokumentasjonskravEgenNæring.`dokumentasjon på arbeidstimer for egen næring ikke tilgjengelig årsak`).besvar(Tekst("Årsak"))
        søknadprosess.boolsk(EgenNæring.`driver du eget gaardsbruk`).besvar(false)
        assertEquals(true, søknadprosess.resultat())
    }

    @Test
    fun `Driver eget gårdsbruk og har dokumentasjon på arbeidstimer`() {
        søknadprosess.boolsk(EgenNæring.`driver du eget gaardsbruk`).besvar(true)
        søknadprosess.boolsk(DokumentasjonskravEgenNæring.`dokumentasjon på arbeidstimer for gårdsbruk tilgjengelig`).besvar(true)
        søknadprosess.dokument(DokumentasjonskravEgenNæring.`dokumentasjon på arbeidstimer for gårdsbruk`).besvar(Dokument(lastOppTidsstempel = LocalDate.now(), "urn:nav:vedlegg:1"))
        søknadprosess.boolsk(EgenNæring.`driver du egen naering`).besvar(false)
        assertEquals(true, søknadprosess.resultat())
    }

    @Test
    fun `Driver eget gårdsbruk men har ikke dokumentasjon på arbeidstimer`() {
        søknadprosess.boolsk(EgenNæring.`driver du eget gaardsbruk`).besvar(true)
        søknadprosess.boolsk(DokumentasjonskravEgenNæring.`dokumentasjon på arbeidstimer for gårdsbruk tilgjengelig`).besvar(false)
        søknadprosess.tekst(DokumentasjonskravEgenNæring.`dokumentasjon på arbeidstimer for gårdsbruk ikke tilgjengelig årsak`).besvar(Tekst("Årsak"))
        søknadprosess.boolsk(EgenNæring.`driver du egen naering`).besvar(false)
        assertEquals(true, søknadprosess.resultat())
    }
}
