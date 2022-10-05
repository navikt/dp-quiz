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
import no.nav.dagpenger.model.regel.inneholder
import no.nav.dagpenger.model.regel.utfylt
import no.nav.dagpenger.model.subsumsjon.DeltreSubsumsjon
import no.nav.dagpenger.model.subsumsjon.alle
import no.nav.dagpenger.model.subsumsjon.deltre
import no.nav.dagpenger.model.subsumsjon.hvisOppfylt
import no.nav.dagpenger.model.subsumsjon.minstEnAv
import no.nav.dagpenger.quiz.mediator.soknad.DslFaktaseksjon

object EgenNæring : DslFaktaseksjon {
    const val `driver du egen næring` = 3001
    const val `egen næring organisasjonsnummer liste` = 3002
    const val `egen næring organisasjonsnummer` = 3003
    const val `egen næring arbeidstimer før` = 3004
    const val `egen næring arbeidstimer nå` = 3005
    const val `driver du eget gårdsbruk` = 3006
    const val `eget gårdsbruk organisasjonsnummer` = 3007
    const val `eget gårdsbruk type gårdsbruk` = 3008
    const val `eget gårdsbruk hvem eier` = 3009
    const val `eget gårdsbruk jeg andel inntekt` = 3010
    const val `eget gårdsbruk ektefelle samboer andel inntekt` = 3011
    const val `eget gårdsbruk andre andel inntekt` = 3012
    const val `eget gårdsbruk arbeidstimer år` = 3013
    const val `eget gårdsbruk arbeidsår for timer` = 3014
    const val `eget gårdsbruk arbeidstimer beregning` = 3015

    override val fakta = listOf(
        boolsk faktum "faktum.driver-du-egen-naering" id `driver du egen næring`,
        heltall faktum "faktum.egen-naering-organisasjonsnummer-liste" id `egen næring organisasjonsnummer liste`
            genererer `egen næring organisasjonsnummer` avhengerAv `driver du egen næring`,
        heltall faktum "faktum.egen-naering-organisasjonsnummer" id `egen næring organisasjonsnummer`,
        desimaltall faktum "faktum.egen-naering-arbeidstimer-for" id `egen næring arbeidstimer før` avhengerAv `driver du egen næring`,
        desimaltall faktum "faktum.egen-naering-arbeidstimer-naa" id `egen næring arbeidstimer nå` avhengerAv `driver du egen næring`,

        boolsk faktum "faktum.driver-du-eget-gaardsbruk" id `driver du eget gårdsbruk`,
        heltall faktum "faktum.eget-gaardsbruk-organisasjonsnummer" id `eget gårdsbruk organisasjonsnummer` avhengerAv `driver du eget gårdsbruk`,
        flervalg faktum "faktum.eget-gaardsbruk-type-gaardsbruk"
            med "svar.dyr"
            med "svar.jord"
            med "svar.skog"
            med "svar.annet" id `eget gårdsbruk type gårdsbruk` avhengerAv `driver du eget gårdsbruk`,
        flervalg faktum "faktum.eget-gaardsbruk-hvem-eier"
            med "svar.selv"
            med "svar.ektefelle-samboer"
            med "svar.andre" id `eget gårdsbruk hvem eier` avhengerAv `driver du eget gårdsbruk`,
        desimaltall faktum "faktum.eget-gaardsbruk-jeg-andel-inntekt" id `eget gårdsbruk jeg andel inntekt` avhengerAv `eget gårdsbruk hvem eier`,
        desimaltall faktum "faktum.eget-gaardsbruk-ektefelle-samboer-andel-inntekt" id `eget gårdsbruk ektefelle samboer andel inntekt` avhengerAv `eget gårdsbruk hvem eier`,
        desimaltall faktum "faktum.eget-gaardsbruk-andre-andel-inntekt" id `eget gårdsbruk andre andel inntekt` avhengerAv `eget gårdsbruk hvem eier`,
        desimaltall faktum "faktum.eget-gaardsbruk-arbeidstimer-aar" id `eget gårdsbruk arbeidstimer år` avhengerAv `driver du eget gårdsbruk`,
        heltall faktum "faktum.eget-gaardsbruk-arbeidsaar-for-timer" id `eget gårdsbruk arbeidsår for timer` avhengerAv `driver du eget gårdsbruk`,
        // @todo: Skal denne være tekst?
        tekst faktum "faktum.eget-gaardsbruk-arbeidstimer-beregning" id `eget gårdsbruk arbeidstimer beregning` avhengerAv `driver du eget gårdsbruk`,
    )

    override fun seksjon(søknad: Søknad) =
        listOf(søknad.seksjon("egen-naering", Rolle.søker, *spørsmålsrekkefølgeForSøker()))

    override fun regeltre(søknad: Søknad): DeltreSubsumsjon = with(søknad) {
        "egennæring".deltre {
            "Egen næring".alle(
                "driver egen næring eller ikke".minstEnAv(
                    boolsk(`driver du egen næring`) er false,
                    boolsk(`driver du egen næring`) er true hvisOppfylt {
                        `næringenes organisasjonsnummer og arbeidstimer`()
                    }
                ),
                "driver eget gårdsbruk eller ikke".minstEnAv(
                    boolsk(`driver du eget gårdsbruk`) er false,
                    boolsk(`driver du eget gårdsbruk`) er true hvisOppfylt {
                        `organisasjonsnummer, type gårdsbruk og eier`()
                    }
                )
            )
        }
    }

    private fun Søknad.`næringenes organisasjonsnummer og arbeidstimer`() =
        "organisasjonsnummer og arbeidstimer".alle(
            generator(`egen næring organisasjonsnummer liste`) har "en eller flere organisasjonsnummer".deltre {
                "organisasjonsnummer for alle næringer".alle(
                    heltall(`egen næring organisasjonsnummer`).utfylt()
                )
            },
            `arbeidstimer for næringen før og nå`()
        )

    private fun Søknad.`arbeidstimer for næringen før og nå`() =
        "spørsmål om arbeidstimer".alle(
            desimaltall(`egen næring arbeidstimer nå`).utfylt(),
            desimaltall(`egen næring arbeidstimer før`).utfylt()
        )

    private fun Søknad.`organisasjonsnummer, type gårdsbruk og eier`() =
        "spørsmål om gårdsbruket".alle(
            heltall(`eget gårdsbruk organisasjonsnummer`).utfylt(),
            flervalg(`eget gårdsbruk type gårdsbruk`).utfylt(),
            "hvem eier gårdsbruket".minstEnAv(
                `søkeren eier selv`(),
                `søkerens ektefelle eller samboer eier`(),
                `noen andre eier`(),
                `arbeidsår, arbeidstimer og forklaring på beregning`()
            )
        )

    private fun Søknad.`søkeren eier selv`() =
        flervalg(`eget gårdsbruk hvem eier`) inneholder Flervalg("faktum.eget-gaardsbruk-hvem-eier.svar.selv") hvisOppfylt {
            desimaltall(`eget gårdsbruk jeg andel inntekt`).utfylt()
        }

    private fun Søknad.`søkerens ektefelle eller samboer eier`() =
        flervalg(`eget gårdsbruk hvem eier`) inneholder Flervalg("faktum.eget-gaardsbruk-hvem-eier.svar.ektefelle-samboer") hvisOppfylt {
            desimaltall(`eget gårdsbruk ektefelle samboer andel inntekt`).utfylt()
        }

    private fun Søknad.`noen andre eier`() =
        flervalg(`eget gårdsbruk hvem eier`) inneholder Flervalg("faktum.eget-gaardsbruk-hvem-eier.svar.andre") hvisOppfylt {
            desimaltall(`eget gårdsbruk andre andel inntekt`).utfylt()
        }

    private fun Søknad.`arbeidsår, arbeidstimer og forklaring på beregning`() = "info arbeidstimer".alle(
        heltall(`eget gårdsbruk arbeidsår for timer`).utfylt(),
        desimaltall(`eget gårdsbruk arbeidstimer år`).utfylt(),
        tekst(`eget gårdsbruk arbeidstimer beregning`).utfylt()
    )

    override val spørsmålsrekkefølgeForSøker = listOf(
        `driver du egen næring`,
        `egen næring organisasjonsnummer liste`,
        `egen næring organisasjonsnummer`,
        `egen næring arbeidstimer nå`,
        `egen næring arbeidstimer før`,
        `driver du eget gårdsbruk`,
        `eget gårdsbruk organisasjonsnummer`,
        `eget gårdsbruk type gårdsbruk`,
        `eget gårdsbruk hvem eier`,
        `eget gårdsbruk jeg andel inntekt`,
        `eget gårdsbruk ektefelle samboer andel inntekt`,
        `eget gårdsbruk andre andel inntekt`,
        `eget gårdsbruk arbeidsår for timer`,
        `eget gårdsbruk arbeidstimer år`,
        `eget gårdsbruk arbeidstimer beregning`
    )
}
