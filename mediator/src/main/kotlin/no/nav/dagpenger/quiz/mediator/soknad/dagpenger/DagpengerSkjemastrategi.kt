package no.nav.dagpenger.quiz.mediator.soknad.dagpenger

import no.nav.dagpenger.model.faktum.Faktum
import no.nav.dagpenger.model.regel.Regel
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.model.subsumsjon.EnkelSubsumsjon
import no.nav.dagpenger.model.visitor.SøknadprosessVisitor
import no.nav.dagpenger.quiz.mediator.behovløsere.Skjemakode
import no.nav.dagpenger.quiz.mediator.behovløsere.SkjemakodeStrategi

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

        override fun preVisit(
            subsumsjon: EnkelSubsumsjon,
            regel: Regel,
            fakta: List<Faktum<*>>,
            lokaltResultat: Boolean?,
            resultat: Boolean?
        ) {
        }
    }
}
