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
        val virkningsdato = 15.januar(2023)

        aldersvurderingsprosess.dato(Paragraf_4_23_alder_vilkår.virkningsdato).besvar(virkningsdato)
        aldersvurderingsprosess.dato(Paragraf_4_23_alder_vilkår.fødselsdato).besvar(virkningsdato.minusYears(66))
        assertTrue(aldersvurderingsprosess.resultat()!!)

        aldersvurderingsprosess.dato(Paragraf_4_23_alder_vilkår.fødselsdato).besvar(virkningsdato.minusYears(68))
        assertFalse(aldersvurderingsprosess.resultat()!!)
    }
}
