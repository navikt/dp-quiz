package no.nav.dagpenger.quiz.mediator.soknad.dagpenger

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.boolsk
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dato
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.desimaltall
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.envalg
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.heltall
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.land
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.periode
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.tekst
import no.nav.dagpenger.model.faktum.Envalg
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.faktum.Søknad.Companion.seksjon
import no.nav.dagpenger.model.regel.er
import no.nav.dagpenger.model.regel.med
import no.nav.dagpenger.model.regel.utfylt
import no.nav.dagpenger.model.subsumsjon.Subsumsjon
import no.nav.dagpenger.model.subsumsjon.alle
import no.nav.dagpenger.model.subsumsjon.bareEnAv
import no.nav.dagpenger.model.subsumsjon.deltre
import no.nav.dagpenger.model.subsumsjon.hvisOppfylt
import no.nav.dagpenger.model.subsumsjon.uansett
import no.nav.dagpenger.quiz.mediator.soknad.DslFaktaseksjon

object Arbeidsforhold : DslFaktaseksjon {
    const val `dagpenger soknadsdato` = 8001//
    const val `type arbeidstid` = 8002//
    const val `arbeidsforhold` = 8003//
    const val `arbeidsforhold navn bedrift` = 8004//
    const val `arbeidsforhold land` = 8005//
    const val `arbeidsforhold endret` = 8006//
    const val `arbeidsforhold kjent antall timer jobbet` = 8007//
    const val `arbeidsforhold antall timer jobbet` = 8008//
    const val `arbeidsforhold tilleggsopplysninger` = 8009//
    const val `arbeidsforhold startdato arbeidsforhold` = 8010
    const val `arbeidsforhold arbeidstid redusert fra dato` = 8011
    const val `arbeidsforhold midlertidig med kontraktfestet sluttdato` = 8012
    const val `arbeidsforhold kontraktfestet sluttdato` = 8013
    const val `arbeidsforhold midlertidig arbeidsforhold oppstartsdato` = 8014
    const val `arbeidsforhold permittertert fra fiskeri naering` = 8015
    const val `arbeidsforhold varighet` = 8016
    const val `arbeidsforhold vet du antall timer foer mistet jobb` = 8017
    const val `arbeidsforhold vet du antall timer foer konkurs` = 8018
    const val `arbeidsforhold vet du antall timer foer kontrakt utgikk` = 8019
    const val `arbeidsforhold vet du antall timer foer du sa opp` = 8020
    const val `arbeidsforhold vet du antall timer foer redusert arbeidstid` = 8021
    const val `arbeidsforhold vet du antall timer foer permittert` = 8022
    const val `arbeidsforhold antall timer dette arbeidsforhold` = 8023
    const val `arbeidsforhold permittert periode` = 8024
    const val `arbeidsforhold permittert prosent` = 8025
    const val `arbeidsforhold vet du lonnsplikt periode` = 8026
    const val `arbeidsforhold naar var lonnsplikt periode` = 8027
    const val `arbeidsforhold aarsak til du sa opp` = 8028
    const val `arbeidsforhold tilbud om forlengelse eller annen stilling` = 8029
    const val `arbeidsforhold svar paa forlengelse eller annen stilling` = 8030
    const val `arbeidsforhold aarsak til ikke akseptert tilbud` = 8031
    const val `arbeidsforhold soke forskudd lonnsgarantimidler` = 8032
    const val `arbeidsforhold soke forskudd lonnsgarantimidler i tillegg til dagpenger` = 8033
    const val `arbeidsforhold godta trekk fra nav av forskudd fra lonnsgarantimidler` = 8034
    const val `arbeidsforhold har sokt om lonnsgarantimidler` = 8035
    const val `arbeidsforhold dekker lonnsgarantiordningen lonnskravet ditt` = 8036
    const val `arbeidsforhold utbetalt lonn etter konkurs` = 8037
    const val `arbeidsforhold siste dag utbetalt for konkurs` = 8038
    const val `arbeidsforhold hva er aarsak til avskjediget` = 8039
    const val `arbeidsforhold vet du aarsak til sagt opp av arbeidsgiver` = 8040
    const val `arbeidsforhold vet du aarsak til redusert arbeidstid` = 8041
    const val `arbeidsforhold midlertidig arbeidsforhold med sluttdato` = 8042
    const val `arbeidsforhold tilbud om annen stilling eller annet sted i norge` = 8043
    const val `arbeidsforhold skift eller turnus` = 8044
    const val `arbeidsforhold rotasjon` = 8045
    const val `arbeidsforhold arbeidsdager siste rotasjon` = 8046
    const val `arbeidsforhold fridager siste rotasjon` = 8047

    override val fakta = listOf(
        dato faktum "faktum.dagpenger-soknadsdato" id `dagpenger soknadsdato`,
        envalg faktum "faktum.type-arbeidstid"
            med "svar.fast"
            med "svar.varierende"
            med "svar.kombinasjon"
            med "svar.ingen-passer" id `type arbeidstid`,
        boolsk faktum "faktum.arbeidsforhold.kjent-antall-timer-jobbet" id `arbeidsforhold kjent antall timer jobbet`,
        desimaltall faktum "faktum.arbeidsforhold.antall-timer-jobbet" id `arbeidsforhold antall timer jobbet`,
        tekst faktum "faktum.arbeidsforhold.tilleggsopplysninger" id `arbeidsforhold tilleggsopplysninger`,
        dato faktum "faktum.arbeidsforhold.startdato-arbeidsforhold" id `arbeidsforhold startdato arbeidsforhold`,
        dato faktum "faktum.arbeidsforhold.arbeidstid-redusert-fra-dato" id `arbeidsforhold arbeidstid redusert fra dato`,
        envalg faktum "faktum.arbeidsforhold.midlertidig-med-kontraktfestet-sluttdato"
            med "svar.ja"
            med "svar.nei"
            med "svar.vet-ikke" id `arbeidsforhold midlertidig med kontraktfestet sluttdato`,
        dato faktum "faktum.arbeidsforhold.kontraktfestet-sluttdato" id `arbeidsforhold kontraktfestet sluttdato`,
        dato faktum "faktum.arbeidsforhold.midlertidig-arbeidsforhold-oppstartsdato" id `arbeidsforhold midlertidig arbeidsforhold oppstartsdato`,
        boolsk faktum "faktum.arbeidsforhold.permittertert-fra-fiskeri-naering" id `arbeidsforhold permittertert fra fiskeri naering`,
        periode faktum "faktum.arbeidsforhold.varighet" id `arbeidsforhold varighet`,
        boolsk faktum "faktum.arbeidsforhold.vet-du-antall-timer-foer-mistet-jobb" id `arbeidsforhold vet du antall timer foer mistet jobb`,
        boolsk faktum "faktum.arbeidsforhold.vet-du-antall-timer-foer-konkurs" id `arbeidsforhold vet du antall timer foer konkurs`,
        boolsk faktum "faktum.arbeidsforhold.vet-du-antall-timer-foer-kontrakt-utgikk" id `arbeidsforhold vet du antall timer foer kontrakt utgikk`,
        boolsk faktum "faktum.arbeidsforhold.vet-du-antall-timer-foer-du-sa-opp" id `arbeidsforhold vet du antall timer foer du sa opp`,
        boolsk faktum "faktum.arbeidsforhold.vet-du-antall-timer-foer-redusert-arbeidstid" id `arbeidsforhold vet du antall timer foer redusert arbeidstid`,
        boolsk faktum "faktum.arbeidsforhold.vet-du-antall-timer-foer-permittert" id `arbeidsforhold vet du antall timer foer permittert`,
        desimaltall faktum "faktum.arbeidsforhold.antall-timer-dette-arbeidsforhold" id `arbeidsforhold antall timer dette arbeidsforhold`,
        periode faktum "faktum.arbeidsforhold.permittert-periode" id `arbeidsforhold permittert periode`,
        heltall faktum "faktum.arbeidsforhold.permittert-prosent" id `arbeidsforhold permittert prosent`,
        boolsk faktum "faktum.arbeidsforhold.vet-du-lonnsplikt-periode" id `arbeidsforhold vet du lonnsplikt periode`,
        periode faktum "faktum.arbeidsforhold.naar-var-lonnsplikt-periode" id `arbeidsforhold naar var lonnsplikt periode`,
        tekst faktum "faktum.arbeidsforhold.aarsak-til-du-sa-opp" id `arbeidsforhold aarsak til du sa opp`,
        boolsk faktum "faktum.arbeidsforhold.tilbud-om-forlengelse-eller-annen-stilling" id `arbeidsforhold tilbud om forlengelse eller annen stilling`,
        envalg faktum "faktum.arbeidsforhold.svar-paa-forlengelse-eller-annen-stilling"
            med "svar.ja"
            med "svar.nei"
            med "svar.ikke-svart" id `arbeidsforhold svar paa forlengelse eller annen stilling`,
        tekst faktum "faktum.arbeidsforhold.aarsak-til-ikke-akseptert-tilbud" id `arbeidsforhold aarsak til ikke akseptert tilbud`,
        boolsk faktum "faktum.arbeidsforhold.soke-forskudd-lonnsgarantimidler" id `arbeidsforhold soke forskudd lonnsgarantimidler`,
        boolsk faktum "faktum.arbeidsforhold.soke-forskudd-lonnsgarantimidler-i-tillegg-til-dagpenger" id `arbeidsforhold soke forskudd lonnsgarantimidler i tillegg til dagpenger`,
        boolsk faktum "faktum.arbeidsforhold.godta-trekk-fra-nav-av-forskudd-fra-lonnsgarantimidler" id `arbeidsforhold godta trekk fra nav av forskudd fra lonnsgarantimidler`,
        envalg faktum "faktum.arbeidsforhold.har-sokt-om-lonnsgarantimidler"
            med "svar.nei"
            med "svar.nei-men-skal"
            med "svar.ja" id `arbeidsforhold har sokt om lonnsgarantimidler`,
        envalg faktum "faktum.arbeidsforhold.dekker-lonnsgarantiordningen-lonnskravet-ditt"
            med "svar.ja"
            med "svar.nei"
            med "svar.vet-ikke" id `arbeidsforhold dekker lonnsgarantiordningen lonnskravet ditt`,
        boolsk faktum "faktum.arbeidsforhold.utbetalt-lonn-etter-konkurs" id `arbeidsforhold utbetalt lonn etter konkurs`,
        dato faktum "faktum.arbeidsforhold.siste-dag-utbetalt-for-konkurs" id `arbeidsforhold siste dag utbetalt for konkurs`,
        tekst faktum "faktum.arbeidsforhold.hva-er-aarsak-til-avskjediget" id `arbeidsforhold hva er aarsak til avskjediget`,
        tekst faktum "faktum.arbeidsforhold.vet-du-aarsak-til-sagt-opp-av-arbeidsgiver" id `arbeidsforhold vet du aarsak til sagt opp av arbeidsgiver`,
        tekst faktum "faktum.arbeidsforhold.vet-du-aarsak-til-redusert-arbeidstid" id `arbeidsforhold vet du aarsak til redusert arbeidstid`,
        envalg faktum "faktum.arbeidsforhold.midlertidig-arbeidsforhold-med-sluttdato"
            med "svar.ja"
            med "svar.nei"
            med "svar.vet-ikke" id `arbeidsforhold midlertidig arbeidsforhold med sluttdato`,
        boolsk faktum "faktum.arbeidsforhold.tilbud-om-annen-stilling-eller-annet-sted-i-norge" id `arbeidsforhold tilbud om annen stilling eller annet sted i norge`,
        boolsk faktum "faktum.arbeidsforhold.skift-eller-turnus" id `arbeidsforhold skift eller turnus`,
        boolsk faktum "faktum.arbeidsforhold.rotasjon" id `arbeidsforhold rotasjon`,
        heltall faktum "faktum.arbeidsforhold.arbeidsdager-siste-rotasjon" id `arbeidsforhold arbeidsdager siste rotasjon`,
        heltall faktum "faktum.arbeidsforhold.fridager-siste-rotasjon" id `arbeidsforhold fridager siste rotasjon`,
        heltall faktum "faktum.arbeidsforhold" id `arbeidsforhold`
            genererer `arbeidsforhold navn bedrift`
            og `arbeidsforhold land`
            og `arbeidsforhold endret`
            og `arbeidsforhold kjent antall timer jobbet`
            og `arbeidsforhold antall timer jobbet`
            og `arbeidsforhold tilleggsopplysninger`
            og `arbeidsforhold startdato arbeidsforhold`
            og `arbeidsforhold arbeidstid redusert fra dato`
            og `arbeidsforhold midlertidig med kontraktfestet sluttdato`
            og `arbeidsforhold kontraktfestet sluttdato`
            og `arbeidsforhold midlertidig arbeidsforhold oppstartsdato`
            og `arbeidsforhold permittertert fra fiskeri naering`
            og `arbeidsforhold varighet`
            og `arbeidsforhold vet du antall timer foer mistet jobb`
            og `arbeidsforhold vet du antall timer foer konkurs`
            og `arbeidsforhold vet du antall timer foer kontrakt utgikk`
            og `arbeidsforhold vet du antall timer foer du sa opp`
            og `arbeidsforhold vet du antall timer foer redusert arbeidstid`
            og `arbeidsforhold vet du antall timer foer permittert`
            og `arbeidsforhold antall timer dette arbeidsforhold`
            og `arbeidsforhold permittert periode`
            og `arbeidsforhold permittert prosent`
            og `arbeidsforhold vet du lonnsplikt periode`
            og `arbeidsforhold naar var lonnsplikt periode`
            og `arbeidsforhold aarsak til du sa opp`
            og `arbeidsforhold tilbud om forlengelse eller annen stilling`
            og `arbeidsforhold svar paa forlengelse eller annen stilling`
            og `arbeidsforhold aarsak til ikke akseptert tilbud`
            og `arbeidsforhold soke forskudd lonnsgarantimidler`
            og `arbeidsforhold soke forskudd lonnsgarantimidler i tillegg til dagpenger`
            og `arbeidsforhold godta trekk fra nav av forskudd fra lonnsgarantimidler`
            og `arbeidsforhold har sokt om lonnsgarantimidler`
            og `arbeidsforhold dekker lonnsgarantiordningen lonnskravet ditt`
            og `arbeidsforhold utbetalt lonn etter konkurs`
            og `arbeidsforhold siste dag utbetalt for konkurs`
            og `arbeidsforhold hva er aarsak til avskjediget`
            og `arbeidsforhold vet du aarsak til sagt opp av arbeidsgiver`
            og `arbeidsforhold vet du aarsak til redusert arbeidstid`
            og `arbeidsforhold midlertidig arbeidsforhold med sluttdato`
            og `arbeidsforhold tilbud om annen stilling eller annet sted i norge`
            og `arbeidsforhold skift eller turnus`
            og `arbeidsforhold rotasjon`
            og `arbeidsforhold arbeidsdager siste rotasjon`
            og `arbeidsforhold fridager siste rotasjon`,
        tekst faktum "faktum.arbeidsforhold.navn-bedrift" id `arbeidsforhold navn bedrift`,
        land faktum "faktum.arbeidsforhold.land" id `arbeidsforhold land`,
        envalg faktum "faktum.arbeidsforhold.endret"//
            med "svar.ikke-endret"
            med "svar.avskjediget"
            med "svar.sagt-opp-av-arbeidsgiver"
            med "svar.arbeidsgiver-konkurs"
            med "svar.kontrakt-utgaatt"
            med "svar.sagt-opp-selv"
            med "svar.redusert-arbeidstid"
            med "svar.permittert" id `arbeidsforhold endret`
    )

    override fun seksjon(søknad: Søknad) = listOf(søknad.seksjon("arbeidsforhold", Rolle.søker, *this.databaseIder()))

    fun regeltre(søknad: Søknad): Subsumsjon = with(søknad){
        "innledende spm".alle(
            dato(`dagpenger soknadsdato`).utfylt(),
            envalg(`type arbeidstid`).utfylt(),
            generator(arbeidsforhold) med "en eller flere arbeidsforhold".deltre {
                "".alle(
                    tekst(`arbeidsforhold navn bedrift`).utfylt(),
                    land(`arbeidsforhold land`).utfylt(),
                    "hvordan arbeidsforholdet har endret seg".bareEnAv(
                        `ikke endret`()
                    )
                )
            }
        )
    }

    private fun Søknad.`ikke endret`() =
        envalg(`arbeidsforhold endret`) er Envalg("faktum.arbeidsforhold.endret.svar.ikke-endret") hvisOppfylt {
            boolsk(`arbeidsforhold kjent antall timer jobbet`) er true hvisOppfylt {
                desimaltall(`arbeidsforhold antall timer jobbet`).utfylt()
            } uansett {
                tekst(`arbeidsforhold tilleggsopplysninger`).utfylt()
            }
        }
}
