package no.nav.dagpenger.quiz.mediator.soknad.dagpenger

import mu.KotlinLogging
import no.nav.dagpenger.model.faktum.Prosessversjon
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.marshalling.FaktumNavBehov
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.model.seksjon.Versjon
import no.nav.dagpenger.model.subsumsjon.Subsumsjon
import no.nav.dagpenger.model.subsumsjon.hvisOppfylt
import no.nav.dagpenger.quiz.mediator.soknad.Prosess
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.Dagpenger.Subsumsjoner.regeltre

internal object Dagpenger {

    private val logger = KotlinLogging.logger { }

    val VERSJON_ID = Prosessversjon(Prosess.Dagpenger, 218)

    fun registrer(registrer: (søknad: Søknad) -> Unit) {
        registrer(søknad)
    }

    private val faktaseksjoner = listOf(
        Bosted,
        Gjenopptak,
        Barnetillegg,
        Arbeidsforhold,
        EøsArbeidsforhold,
        EgenNæring,
        Verneplikt,
        AndreYtelser,
        ReellArbeidssoker,
        Utdanning,
        Tilleggsopplysninger,
        DokumentasjonsKrav
    )

    private val alleFakta = flatMapAlleFakta()
    private val alleSeksjoner = flatMapAlleSeksjoner()

    private val søknad: Søknad
        get() = Søknad(
            VERSJON_ID,
            *alleFakta
        )

    private val søknadsprosess: Søknadprosess = Søknadprosess(*alleSeksjoner)

    object Subsumsjoner {
        val regeltre: Subsumsjon = with(søknad) {
            Bosted.regeltre(søknad).hvisOppfylt {
                Gjenopptak.regeltre(søknad).hvisOppfylt {
                    Barnetillegg.regeltre(søknad).hvisOppfylt {
                        Arbeidsforhold.regeltre(søknad).hvisOppfylt {
                            EøsArbeidsforhold.regeltre(søknad).hvisOppfylt {
                                EgenNæring.regeltre(søknad).hvisOppfylt {
                                    Verneplikt.regeltre(søknad).hvisOppfylt {
                                        AndreYtelser.regeltre(søknad).hvisOppfylt {
                                            Utdanning.regeltre(søknad).hvisOppfylt {
                                                ReellArbeidssoker.regeltre(søknad).hvisOppfylt {
                                                    Tilleggsopplysninger.regeltre(søknad).hvisOppfylt {
                                                        DokumentasjonsKrav.regeltre(søknad)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private val faktumNavBehov =
        FaktumNavBehov(
            mapOf(
                Barnetillegg.`barn liste` to "Barn",
            )
        )

    @Suppress("unused")
    private val versjon = Versjon.Bygger(
        prototypeSøknad = søknad,
        prototypeSubsumsjon = regeltre,
        prototypeUserInterfaces = mapOf(
            Versjon.UserInterfaceType.Web to søknadsprosess
        ),
        faktumNavBehov = faktumNavBehov
    ).registrer().also {
        logger.info { "\n\n\nREGISTRERT versjon id $VERSJON_ID \n\n\n\n" }
    }

    private fun flatMapAlleFakta() = faktaseksjoner.flatMap { seksjon ->
        seksjon.fakta().toList()
    }.toTypedArray()

    private fun flatMapAlleSeksjoner() = faktaseksjoner.map { faktaSeksjon ->
        faktaSeksjon.seksjon(søknad)
    }.flatten().toTypedArray()
}
