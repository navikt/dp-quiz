package no.nav.dagpenger.quiz.mediator.integration


import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntekt.arbeidsforhold
import org.junit.jupiter.api.Test

class RettighetsTypeISøknadTest {

    private val søknad = Søknad(
        0,
        *arbeidsforhold
    )

 /*      heltall faktum "antall arbedigsforhold" id 21 genererer 22 og 23 og 24 og 25 og 26,
        ja nei "Permitert ordinær" id 22,
        ja nei "Dagpenger ordinær" id 23,
        ja nei "Lærling sluttårsak" id 24,
        ja nei "Lønnsgaranti" id 25,
        ja nei "Permitert fra fiskeindustrien" id 26,
        ja nei "Godkjenning rettighet" id 27 avhengerAv 21     */



    private val antallArbeidsforhold = søknad generator 21

    @Test
    fun `Håndterer rettighetstyper`() {

    }

    private fun fakta(id:Int, indeks: Int) = søknad ja "$id.$indeks"


}