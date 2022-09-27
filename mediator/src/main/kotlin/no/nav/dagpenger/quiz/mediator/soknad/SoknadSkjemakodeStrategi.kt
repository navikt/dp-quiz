package no.nav.dagpenger.quiz.mediator.soknad

import no.nav.dagpenger.model.faktum.Prosessversjon
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.model.visitor.SøknadprosessVisitor
import no.nav.dagpenger.quiz.mediator.behovløsere.Skjemakode
import no.nav.dagpenger.quiz.mediator.behovløsere.SkjemakodeStrategi
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.DagpengerSkjemastrategi
import no.nav.dagpenger.quiz.mediator.soknad.innsending.Innsending
import no.nav.dagpenger.quiz.mediator.soknad.innsending.InnsendingSkjemastrategi
import java.util.UUID

internal class SoknadSkjemakodeStrategi : SkjemakodeStrategi {

    override fun skjemakode(søknadprosess: Søknadprosess): Skjemakode {
        return SkjemastrategiVelger(søknadprosess).skjemakodeStrategi()
    }

    private class SkjemastrategiVelger(private val søknadprosess: Søknadprosess) : SøknadprosessVisitor {

        private lateinit var skjemakode: Skjemakode

        init {
            søknadprosess.accept(this)
        }

        override fun preVisit(søknad: Søknad, prosessVersjon: Prosessversjon, uuid: UUID) {
            skjemakode = when (prosessVersjon.prosessnavn) {
                Prosess.Dagpenger -> DagpengerSkjemastrategi().skjemakode(søknadprosess)
                Prosess.Innsending -> InnsendingSkjemastrategi().skjemakode(søknadprosess)
                else -> throw IllegalArgumentException("Har ikke laget skjemakodestrategi for ${prosessVersjon.prosessnavn}")
            }
        }

        fun skjemakodeStrategi(): Skjemakode = skjemakode
    }
}
