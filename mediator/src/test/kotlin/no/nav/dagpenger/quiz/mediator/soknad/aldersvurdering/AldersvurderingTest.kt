package no.nav.dagpenger.quiz.mediator.soknad.aldersvurdering

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
    fun `Aldersvurder bruker 67 år`() {
        val virkningsdato = 15.januar(2023)

        aldersvurderingsprosess.heltall(Aldersvurdering.aldersgrense).besvar(67)
        aldersvurderingsprosess.dato(Aldersvurdering.virkningsdato).besvar(virkningsdato)
        aldersvurderingsprosess.dato(Aldersvurdering.fødselsdato).besvar(virkningsdato.minusYears(66))
        assertTrue(aldersvurderingsprosess.resultat()!!)

        aldersvurderingsprosess.dato(Aldersvurdering.fødselsdato).besvar(virkningsdato.minusYears(68))
        assertFalse(aldersvurderingsprosess.resultat()!!)
    }
}
