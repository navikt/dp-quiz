package no.nav.dagpenger.quiz.mediator.soknad.dagpenger.v248

import mu.KotlinLogging
import no.nav.dagpenger.model.faktum.Fakta
import no.nav.dagpenger.model.faktum.FaktaVersjon
import no.nav.dagpenger.model.faktum.Prosessversjon
import no.nav.dagpenger.model.marshalling.FaktumNavBehov
import no.nav.dagpenger.model.seksjon.Utredningsprosess
import no.nav.dagpenger.model.seksjon.Versjon
import no.nav.dagpenger.model.subsumsjon.Subsumsjon
import no.nav.dagpenger.model.subsumsjon.hvisOppfylt
import no.nav.dagpenger.model.subsumsjon.uansett
import no.nav.dagpenger.quiz.mediator.soknad.Prosess
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.v248.Dagpenger.Subsumsjoner.regeltre

internal object Dagpenger {
    private val logger = KotlinLogging.logger { }

    /**
     * PS! Når vi går ut i prod må vi begynne å kopiere hele oppsettet hver gang vi øker denne variabelen.
     *
     * Dette for at innsendte søknader fortsatt skal kunne lastes, uten å bli migrert fram.
     */
    val VERSJON_ID = FaktaVersjon(Prosess.Dagpenger, 248)

    fun registrer(registrer: (prototype: Fakta) -> Unit) {
        registrer(prototypeFakta)
    }

    private val faktaseksjoner = listOf(
        Bosted,
        DinSituasjon,
        EøsArbeidsforhold,
        EgenNæring,
        Verneplikt,
        AndreYtelser,
        Utdanning,
        Barnetillegg,
        ReellArbeidssoker,
        Tilleggsopplysninger
    )
    private val alleFakta = flatMapAlleFakta()
    private val alleSeksjoner = flatMapAlleSeksjoner()
    private val prototypeFakta: Fakta
        get() = Fakta(
            VERSJON_ID,
            *alleFakta
        )

    object Subsumsjoner {
        val regeltre: Subsumsjon = with(prototypeFakta) {
            Bosted.regeltre(this).hvisOppfylt {
                DinSituasjon.regeltre(this).uansett {
                    EøsArbeidsforhold.regeltre(this).uansett {
                        EgenNæring.regeltre(this).hvisOppfylt {
                            Verneplikt.regeltre(this).hvisOppfylt {
                                AndreYtelser.regeltre(this).hvisOppfylt {
                                    Utdanning.regeltre(this).hvisOppfylt {
                                        Barnetillegg.regeltre(this).hvisOppfylt {
                                            ReellArbeidssoker.regeltre(this).hvisOppfylt {
                                                Tilleggsopplysninger.regeltre(this)
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
                Barnetillegg.`barn liste register` to "Barn"
            )
        )

    private val søknadId = Prosessversjon(Prosess.Dagpenger, "søknad")
    private val dokumentasjonsprosessId = Prosessversjon(Prosess.Dagpenger, "søknad")
    private val søknadprosess = Utredningsprosess(*alleSeksjoner)
    private val dokumentasjonsprosess = Utredningsprosess(*alleSeksjoner)

    init {
        Versjon.Bygger(
            prototypeFakta = prototypeFakta,
            prototypeSubsumsjon = regeltre,
            utredningsprosess = søknadprosess,
            faktumNavBehov = faktumNavBehov,
            prosessversjon = søknadId
        ).registrer().also {
            logger.info { "\n\n\nREGISTRERT versjon id $VERSJON_ID \n\n\n\n" }
        }

        Versjon.Bygger(
            prototypeFakta = prototypeFakta,
            prototypeSubsumsjon = regeltre,
            utredningsprosess = dokumentasjonsprosess,
            faktumNavBehov = faktumNavBehov,
            prosessversjon = dokumentasjonsprosessId
        ).registrer().also {
            logger.info { "\n\n\nREGISTRERT versjon id $VERSJON_ID \n\n\n\n" }
        }
    }

    private fun flatMapAlleFakta() = faktaseksjoner.flatMap { seksjon ->
        seksjon.fakta().toList()
    }.toTypedArray()

    private fun flatMapAlleSeksjoner() = faktaseksjoner.map { faktaSeksjon ->
        faktaSeksjon.seksjon(prototypeFakta)
    }.flatten().toTypedArray()
}
