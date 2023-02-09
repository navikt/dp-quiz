package no.nav.dagpenger.quiz.mediator.soknad.dagpenger

import no.nav.dagpenger.model.faktum.Envalg
import no.nav.dagpenger.model.faktum.Fakta
import no.nav.dagpenger.model.faktum.Faktaversjon
import no.nav.dagpenger.model.faktum.Faktum
import no.nav.dagpenger.model.regel.Regel
import no.nav.dagpenger.model.seksjon.Utredningsprosess
import no.nav.dagpenger.model.subsumsjon.EnkelSubsumsjon
import no.nav.dagpenger.model.visitor.UtredningsprosessVisitor
import no.nav.dagpenger.quiz.mediator.behovløsere.MetadataStrategi
import no.nav.dagpenger.quiz.mediator.behovløsere.MetadataStrategi.Metadata
import no.nav.dagpenger.quiz.mediator.soknad.Prosessfakta
import java.util.UUID

class DagpengerMetadataStrategi : MetadataStrategi {
    override fun metadata(utredningsprosess: Utredningsprosess): Metadata {
        return DagpengerSkjemakodeFinner(utredningsprosess).skjemaKode()
    }

    private class DagpengerSkjemakodeFinner(utredningsprosess: Utredningsprosess) : UtredningsprosessVisitor {
        private var permittert: Boolean = false
        private var gjenopptak: Boolean = false

        init {
            utredningsprosess.accept(this)
        }

        fun skjemaKode(): Metadata {
            return when (permittert) {
                true -> when (gjenopptak) {
                    true -> Metadata("04-16.04")
                    else -> Metadata("04-01.04")
                }

                else -> when (gjenopptak) {
                    true -> Metadata("04-16.03")
                    else -> Metadata("04-01.03")
                }
            }
        }

        override fun preVisit(fakta: Fakta, faktaversjon: Faktaversjon, uuid: UUID) {
            require(faktaversjon.faktatype == Prosessfakta.Dagpenger) { "Kan kun håndtere ${Prosessfakta.Dagpenger.name}, var ${faktaversjon.faktatype}" }
        }

        override fun preVisit(
            subsumsjon: EnkelSubsumsjon,
            regel: Regel,
            fakta: List<Faktum<*>>,
            lokaltResultat: Boolean?,
            resultat: Boolean?,
        ) {
            // TODO: vi vil gjerne identifisere subsumsjoner i stede for å bruke faktaene
            if (!gjenopptak) {
                val g = fakta.filter { it.id.contains(DinSituasjon.`mottatt dagpenger siste 12 mnd`.toString()) }
                    .filter { it.erBesvart() }
                    .filter { it.svar() == Envalg("faktum.mottatt-dagpenger-siste-12-mnd.svar.ja") }
                gjenopptak = g.isNotEmpty()
            }

            if (!permittert) {
                val arbeidsforholdFakta =
                    fakta.filter { it.id.contains(DinSituasjon.`arbeidsforhold endret`.toString()) }
                        .filter { it.erBesvart() }
                        .filter { it.svar() == Envalg("faktum.arbeidsforhold.endret.svar.permittert") }

                permittert = arbeidsforholdFakta.isNotEmpty() && lokaltResultat == true
            }
        }
    }
}
