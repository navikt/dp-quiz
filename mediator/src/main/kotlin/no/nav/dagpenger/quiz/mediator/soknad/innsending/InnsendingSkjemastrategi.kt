package no.nav.dagpenger.quiz.mediator.soknad.innsending

import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.quiz.mediator.behovløsere.Skjemakode
import no.nav.dagpenger.quiz.mediator.behovløsere.SkjemakodeStrategi

class InnsendingSkjemastrategi : SkjemakodeStrategi {
    override fun skjemakode(søknadprosess: Søknadprosess) = Skjemakode("04-01.03")
}