package no.nav.dagpenger.quiz.mediator.soknad.dagpenger

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.boolsk
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.desimaltall
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.flervalg
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.heltall
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.tekst
import no.nav.dagpenger.model.faktum.Flervalg
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.faktum.Søknad.Companion.seksjon
import no.nav.dagpenger.model.regel.er
import no.nav.dagpenger.model.regel.har
import no.nav.dagpenger.model.regel.utfylt
import no.nav.dagpenger.model.subsumsjon.Subsumsjon
import no.nav.dagpenger.model.subsumsjon.alle
import no.nav.dagpenger.model.subsumsjon.deltre
import no.nav.dagpenger.model.subsumsjon.hvisOppfylt
import no.nav.dagpenger.model.subsumsjon.minstEnAv
import no.nav.dagpenger.quiz.mediator.soknad.DslFaktaseksjon

object EgenNæring : DslFaktaseksjon {
    const val `driver du egen naering` = 3001
    const val `egen naering organisasjonsnummer liste` = 3002
    const val `egen naering organisasjonsnummer` = 3003
    const val `egen naering arbeidstimer for` = 3004
    const val `egen naering arbeidstimer naa` = 3005
    const val `driver du eget gaardsbruk` = 3006
    const val `eget gaardsbruk organisasjonsnummer` = 3007
    const val `eget gaardsbruk type gaardsbruk` = 3008
    const val `eget gaardsbruk hvem eier` = 3009
    const val `eget gaardsbruk jeg andel inntekt` = 3010
    const val `eget gaardsbruk ektefelle samboer andel inntekt` = 3011
    const val `eget gaardsbruk andre andel inntekt` = 3012
    const val `eget gaardsbruk arbeidstimer aar` = 3013
    const val `eget gaardsbruk arbeidsaar for timer` = 3014
    const val `eget gaardsbruk arbeidstimer beregning` = 3015

    override val fakta = listOf(
        boolsk faktum "faktum.driver-du-egen-naering" id `driver du egen naering`,
        heltall faktum "faktum.egen-naering-organisasjonsnummer-liste" id `egen naering organisasjonsnummer liste`
            genererer `egen naering organisasjonsnummer`,
        heltall faktum "faktum.egen-naering-organisasjonsnummer" id `egen naering organisasjonsnummer`,
        desimaltall faktum "faktum.egen-naering-arbeidstimer-for" id `egen naering arbeidstimer for`,
        desimaltall faktum "faktum.egen-naering-arbeidstimer-naa" id `egen naering arbeidstimer naa`,
        boolsk faktum "faktum.driver-du-eget-gaardsbruk" id `driver du eget gaardsbruk`,
        heltall faktum "faktum.eget-gaardsbruk-organisasjonsnummer" id `eget gaardsbruk organisasjonsnummer`,
        flervalg faktum "faktum.eget-gaardsbruk-type-gaardsbruk"
            med "svar.dyr"
            med "svar.jord"
            med "svar.skog"
            med "svar.annet" id `eget gaardsbruk type gaardsbruk`,
        flervalg faktum "faktum.eget-gaardsbruk-hvem-eier"
            med "svar.selv"
            med "svar.ektefelle-samboer"
            med "svar.andre" id `eget gaardsbruk hvem eier`,
        desimaltall faktum "faktum.eget-gaardsbruk-jeg-andel-inntekt" id `eget gaardsbruk jeg andel inntekt`,
        desimaltall faktum "faktum.eget-gaardsbruk-ektefelle-samboer-andel-inntekt" id `eget gaardsbruk ektefelle samboer andel inntekt`,
        desimaltall faktum "faktum.eget-gaardsbruk-andre-andel-inntekt" id `eget gaardsbruk andre andel inntekt`,
        desimaltall faktum "faktum.eget-gaardsbruk-arbeidstimer-aar" id `eget gaardsbruk arbeidstimer aar`,
        heltall faktum "faktum.eget-gaardsbruk-arbeidsaar-for-timer" id `eget gaardsbruk arbeidsaar for timer`,
        // @todo: Skal denne være tekst?
        tekst faktum "faktum.eget-gaardsbruk-arbeidstimer-beregning" id `eget gaardsbruk arbeidstimer beregning`

    )

    override fun seksjon(søknad: Søknad) = listOf(søknad.seksjon("egen-naering", Rolle.søker, *this.databaseIder()))

    fun regeltre(søknad: Søknad): Subsumsjon = with(søknad) {
        "driver egen næring".minstEnAv(
            boolsk(`driver du egen naering`) er false,
            boolsk(`driver du egen naering`) er true hvisOppfylt {
                `næringenes organisasjonsnummer og arbeidstimer`()
            }
        ).hvisOppfylt {
            "driver eget gårdsbruk".minstEnAv(
                boolsk(`driver du eget gaardsbruk`) er false,
                boolsk(`driver du eget gaardsbruk`) er true hvisOppfylt {
                    `informasjon om gårdsbruk`()
                }
            )
        }
    }

    private fun Søknad.`næringenes organisasjonsnummer og arbeidstimer`() =
        generator(`egen naering organisasjonsnummer liste`) har "en eller flere organisasjonsnummer".deltre {
            "organisasjonsnummer for alle næringer".alle(
                heltall(`egen naering organisasjonsnummer`).utfylt() hvisOppfylt {
                    `arbeidstimer for næringen før og nå`()
                }
            )
        }

    private fun Søknad.`arbeidstimer for næringen før og nå`() =
        "arbeidstimer i egen næring før og nå".alle(
            desimaltall(`egen naering arbeidstimer naa`).utfylt(),
            desimaltall(`egen naering arbeidstimer for`).utfylt()
        )

    private fun Søknad.`informasjon om gårdsbruk`() =
        heltall(`eget gaardsbruk organisasjonsnummer`).utfylt().hvisOppfylt {
            flervalg(`eget gaardsbruk type gaardsbruk`).utfylt() hvisOppfylt {
                "hvem eier gårdsbruket".minstEnAv(
                    `søkeren eier selv`(),
                    `søkerens ektefelle eller samboer eier`(),
                    `noen andre eier`(),
                ).hvisOppfylt {
                    `informasjon om arbeidstimer`()
                }
            }
        }

    private fun Søknad.`søkeren eier selv`() =
        flervalg(`eget gaardsbruk hvem eier`) er Flervalg("faktum.eget-gaardsbruk-hvem-eier.svar.selv") hvisOppfylt {
            desimaltall(`eget gaardsbruk jeg andel inntekt`).utfylt()
        }

    private fun Søknad.`søkerens ektefelle eller samboer eier`() =
        flervalg(`eget gaardsbruk hvem eier`) er Flervalg("faktum.eget-gaardsbruk-hvem-eier.svar.ektefelle-samboer") hvisOppfylt {
            desimaltall(`eget gaardsbruk ektefelle samboer andel inntekt`).utfylt()
        }

    private fun Søknad.`noen andre eier`() =
        flervalg(`eget gaardsbruk hvem eier`) er Flervalg("faktum.eget-gaardsbruk-hvem-eier.svar.andre") hvisOppfylt {
            desimaltall(`eget gaardsbruk andre andel inntekt`).utfylt()
        }

    private fun Søknad.`informasjon om arbeidstimer`() = "info arbeidstimer".alle(
        heltall(`eget gaardsbruk arbeidsaar for timer`).utfylt(),
        desimaltall(`eget gaardsbruk arbeidstimer aar`).utfylt(),
        tekst(`eget gaardsbruk arbeidstimer beregning`).utfylt()
    )
}
