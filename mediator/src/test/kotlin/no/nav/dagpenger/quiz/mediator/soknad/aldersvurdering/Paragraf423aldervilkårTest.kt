package no.nav.dagpenger.quiz.mediator.soknad.aldersvurdering

import no.nav.dagpenger.model.faktum.Dokument
import no.nav.dagpenger.model.faktum.Prosessversjon
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.helpers.januar
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.quiz.mediator.helpers.testSøknadprosess
import no.nav.dagpenger.quiz.mediator.soknad.Prosess
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.UUID.randomUUID

internal class Paragraf423aldervilkårTest {

    private val søknad = Søknad(Prosessversjon(Prosess.Paragraf_4_23_alder, -1), *Paragraf_4_23_alder_vilkår.fakta())
    private lateinit var aldersvurderingsprosess: Søknadprosess

    @BeforeEach
    fun setup() {
        aldersvurderingsprosess = søknad.testSøknadprosess(
            Paragraf_4_23_alder_vilkår.regeltre(søknad)
        ) {
            Paragraf_4_23_alder_vilkår.seksjon(this)
        }
    }

    @Test
    fun `Aldersvurder bruker 67 år`() {
        aldersvurderingsprosess.dokument(Paragraf_4_23_alder_vilkår.innsendtSøknadId)
            .besvar(Dokument(LocalDateTime.now(), "urn:soknadid:${randomUUID()}"))
        aldersvurderingsprosess.dato(Paragraf_4_23_alder_vilkår.søknadInnsendtDato).besvar(15.januar(2023))
        aldersvurderingsprosess.dato(Paragraf_4_23_alder_vilkår.ønskerDagpengerFraDato).besvar(14.januar(2023))

        aldersvurderingsprosess.dato(Paragraf_4_23_alder_vilkår.fødselsdato).besvar(15.januar(2023).minusYears(66))
        assertTrue(aldersvurderingsprosess.resultat()!!)

        aldersvurderingsprosess.dato(Paragraf_4_23_alder_vilkår.fødselsdato).besvar(15.januar(2023).minusYears(68))
        assertFalse(aldersvurderingsprosess.resultat()!!)
    }
}
