package no.nav.dagpenger.quiz.mediator.soknad.aldersvurdering

import no.nav.dagpenger.model.faktum.Prosessversjon
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.quiz.mediator.helpers.testSøknadprosess
import no.nav.dagpenger.quiz.mediator.soknad.Prosess
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class AldersvurderingTest {

    private val søknad = Søknad(Prosessversjon(Prosess.Aldersvurdering, -1), *Aldersvurdering.fakta())
    private lateinit var aldersvurderingsprosess: Søknadprosess

    @BeforeEach
    fun setup() {
        aldersvurderingsprosess = søknad.testSøknadprosess(
            Aldersvurdering.regeltre(søknad)
        ) {
            Aldersvurdering.seksjon(this)
        }
    }

    @Test
    fun `Aldersvurder bruker over 67 år`() {
        aldersvurderingsprosess.heltall(Aldersvurdering.alder).besvar(66)
        assertTrue(aldersvurderingsprosess.resultat()!!)
        aldersvurderingsprosess.heltall(Aldersvurdering.alder).besvar(67)
        assertFalse(aldersvurderingsprosess.resultat()!!)
    }
}