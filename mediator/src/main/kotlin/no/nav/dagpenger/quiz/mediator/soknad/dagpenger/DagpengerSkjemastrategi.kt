package no.nav.dagpenger.quiz.mediator.soknad.dagpenger

import no.nav.dagpenger.model.faktum.Envalg
import no.nav.dagpenger.model.faktum.Faktum
import no.nav.dagpenger.model.faktum.Prosessversjon
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.regel.Regel
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.model.subsumsjon.EnkelSubsumsjon
import no.nav.dagpenger.model.visitor.SøknadprosessVisitor
import no.nav.dagpenger.quiz.mediator.behovløsere.Skjemakode
import no.nav.dagpenger.quiz.mediator.behovløsere.SkjemakodeStrategi
import no.nav.dagpenger.quiz.mediator.soknad.Prosess
import java.util.UUID

class DagpengerSkjemastrategi : SkjemakodeStrategi {
    override fun skjemakode(søknadprosess: Søknadprosess): Skjemakode {
        return DagpengerSkjemakodeFinner(søknadprosess).skjemaKode()
    }

    private class DagpengerSkjemakodeFinner(søknadprosess: Søknadprosess) : SøknadprosessVisitor {

        private var permittert: Boolean = false

        init {
            søknadprosess.accept(this)
        }

        fun skjemaKode(): Skjemakode {
            return when (permittert) {
                true -> Skjemakode("Søknad om dagpenger ved permittering", "04-01.04")
                else -> Skjemakode("Søknad om dagpenger (ikke permittert)", "04-01.03")
            }
        }

        override fun preVisit(søknad: Søknad, prosessVersjon: Prosessversjon, uuid: UUID) {
            require(prosessVersjon.prosessnavn == Prosess.Dagpenger) { "Kan kun håndtere ${Prosess.Dagpenger.name}, var ${prosessVersjon.prosessnavn}" }
        }

        override fun preVisit(
            subsumsjon: EnkelSubsumsjon,
            regel: Regel,
            fakta: List<Faktum<*>>,
            lokaltResultat: Boolean?,
            resultat: Boolean?
        ) {
            if (!permittert) {
                val arbeidsforholdFakta = fakta.filter { it.id.contains(Arbeidsforhold.`arbeidsforhold endret`.toString()) }
                    .filter { it.erBesvart() }
                    .filter { it.svar() == Envalg("faktum.arbeidsforhold.endret.svar.permittert") }

                permittert = arbeidsforholdFakta.isNotEmpty() && lokaltResultat == true
            }
        }
    }
}
