package no.nav.dagpenger.quiz.mediator.soknad.dagpenger

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.boolsk
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dato
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.desimaltall
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dokument
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
import no.nav.dagpenger.model.regel.inneholder
import no.nav.dagpenger.model.regel.med
import no.nav.dagpenger.model.regel.utfylt
import no.nav.dagpenger.model.subsumsjon.DeltreSubsumsjon
import no.nav.dagpenger.model.subsumsjon.alle
import no.nav.dagpenger.model.subsumsjon.bareEnAv
import no.nav.dagpenger.model.subsumsjon.deltre
import no.nav.dagpenger.model.subsumsjon.godkjentAv
import no.nav.dagpenger.model.subsumsjon.hvisIkkeOppfylt
import no.nav.dagpenger.model.subsumsjon.hvisOppfylt
import no.nav.dagpenger.model.subsumsjon.minstEnAv
import no.nav.dagpenger.model.subsumsjon.sannsynliggjøresAv
import no.nav.dagpenger.quiz.mediator.soknad.DslFaktaseksjon

object Arbeidsforhold : DslFaktaseksjon {
    const val `dagpenger søknadsdato` = 8001
    const val `type arbeidstid` = 8002
    const val arbeidsforhold = 8003
    const val `arbeidsforhold navn bedrift` = 8004
    const val `arbeidsforhold land` = 8005
    const val `arbeidsforhold endret` = 8006
    const val `arbeidsforhold kjent antall timer jobbet` = 8007
    const val `arbeidsforhold antall timer jobbet` = 8008
    const val `arbeidsforhold tilleggsopplysninger` = 8009
    const val `arbeidsforhold startdato arbeidsforhold` = 8010
    const val `arbeidsforhold arbeidstid redusert fra dato` = 8011
    const val `arbeidsforhold midlertidig med kontraktfestet sluttdato` = 8012
    const val `arbeidsforhold kontraktfestet sluttdato` = 8013
    const val `arbeidsforhold midlertidig arbeidsforhold oppstartsdato` = 8014
    const val `arbeidsforhold permittert fra fiskeri næring` = 8015
    const val `arbeidsforhold varighet` = 8016
    const val `arbeidsforhold vet du antall timer før mistet jobb` = 8017
    const val `arbeidsforhold vet du antall timer før konkurs` = 8018
    const val `arbeidsforhold vet du antall timer før kontrakt utgikk` = 8019
    const val `arbeidsforhold vet du antall timer før du sa opp` = 8020
    const val `arbeidsforhold vet du antall timer før redusert arbeidstid` = 8021
    const val `arbeidsforhold vet du antall timer før permittert` = 8022
    const val `arbeidsforhold antall timer dette arbeidsforhold` = 8023
    const val `arbeidsforhold permittert periode` = 8024
    const val `arbeidsforhold permittert prosent` = 8025
    const val `arbeidsforhold vet du lønnsplikt periode` = 8026
    const val `arbeidsforhold når var lønnsplikt periode` = 8027
    const val `arbeidsforhold årsak til du sa opp` = 8028
    const val `arbeidsforhold tilbud om forlengelse eller annen stilling` = 8029
    const val `arbeidsforhold svar på forlengelse eller annen stilling` = 8030
    const val `arbeidsforhold årsak til ikke akseptert tilbud` = 8031
    const val `arbeidsforhold søke forskudd lønnsgarantimidler` = 8032
    const val `arbeidsforhold søke forskudd lønnsgarantimidler i tillegg til dagpenger` = 8033
    const val `arbeidsforhold godta trekk fra nav av forskudd fra lønnsgarantimidler` = 8034
    const val `arbeidsforhold har søkt om lønnsgarantimidler` = 8035
    const val `arbeidsforhold dekker lønnsgarantiordningen lønnskravet ditt` = 8036
    const val `arbeidsforhold utbetalt lønn etter konkurs` = 8037
    const val `arbeidsforhold siste dag utbetalt for konkurs` = 8038
    const val `arbeidsforhold hva er årsak til avskjediget` = 8039
    const val `arbeidsforhold vet du årsak til sagt opp av arbeidsgiver` = 8040
    const val `arbeidsforhold vet du årsak til redusert arbeidstid` = 8041
    const val `arbeidsforhold midlertidig arbeidsforhold med sluttdato` = 8042
    const val `arbeidsforhold tilbud om annen stilling eller annet sted i norge` = 8043
    const val `arbeidsforhold skift eller turnus` = 8044
    const val `arbeidsforhold rotasjon` = 8045
    const val `arbeidsforhold arbeidsdager siste rotasjon` = 8046
    const val `arbeidsforhold fridager siste rotasjon` = 8047
    const val `arbeidsforhold har tilleggsopplysninger` = 8048
    const val `gjenopptak jobbet siden sist du fikk dagpenger` = 8049
    const val `gjenopptak årsak til stans av dagpenger` = 8050
    const val `gjenopptak søknadsdato` = 8051
    const val `gjenopptak endringer i arbeidsforhold siden sist` = 8052
    const val `gjenopptak ønsker ny beregning av dagpenger` = 8053
    const val `gjenopptak ønsker å få fastsatt ny vanlig arbeidstid` = 8054

    const val `dokumentasjon arbeidsavtale` = 8055
    const val `godkjenning dokumentasjon arbeidsavtale` = 8056
    const val `dokumentasjon helt eller delvis avsluttet arbeidsforhold` = 8057
    const val `godkjenning dokumentasjon helt eller delvis avsluttet arbeidsforhold` = 8058
    const val `dokumentasjon timeliste for rotasjon` = 8059
    const val `godkjenning dokumentasjon timeliste for rotasjon` = 8060
    const val `dokumentasjon brev fra bobestyrer eller konkursforvalter` = 8061
    const val `godkjenning dokumentasjon brev fra bobestyrer eller konkursforvalter` = 8062
    const val `dokumentasjon ny arbeidsavtale` = 8063
    const val `godkjenning dokumentasjon ny arbeidsavtale` = 8064
    const val `dokumentasjon varsel om permittering` = 8065
    const val `godkjenning dokumentasjon varsel om permittering` = 8066

    override val fakta = listOf(
        dato faktum "faktum.dagpenger-soknadsdato" id `dagpenger søknadsdato`,
        envalg faktum "faktum.type-arbeidstid"
            med "svar.fast"
            med "svar.varierende"
            med "svar.kombinasjon"
            med "svar.ingen-passer" id `type arbeidstid`,
        boolsk faktum "faktum.arbeidsforhold.kjent-antall-timer-jobbet" id `arbeidsforhold kjent antall timer jobbet`
            avhengerAv `arbeidsforhold endret`,
        desimaltall faktum "faktum.arbeidsforhold.antall-timer-jobbet" id `arbeidsforhold antall timer jobbet`
            avhengerAv `arbeidsforhold kjent antall timer jobbet`,
        boolsk faktum "faktum.arbeidsforhold.har-tilleggsopplysninger" id `arbeidsforhold har tilleggsopplysninger`
            avhengerAv `arbeidsforhold endret`,
        tekst faktum "faktum.arbeidsforhold.tilleggsopplysninger" id `arbeidsforhold tilleggsopplysninger`
            avhengerAv `arbeidsforhold har tilleggsopplysninger`,
        dato faktum "faktum.arbeidsforhold.startdato-arbeidsforhold" id `arbeidsforhold startdato arbeidsforhold`
            avhengerAv `arbeidsforhold endret`,
        dato faktum "faktum.arbeidsforhold.arbeidstid-redusert-fra-dato" id `arbeidsforhold arbeidstid redusert fra dato`
            avhengerAv `arbeidsforhold endret`,
        envalg faktum "faktum.arbeidsforhold.midlertidig-med-kontraktfestet-sluttdato"
            med "svar.ja"
            med "svar.nei"
            med "svar.vet-ikke" id `arbeidsforhold midlertidig med kontraktfestet sluttdato`
            avhengerAv `arbeidsforhold endret`,
        dato faktum "faktum.arbeidsforhold.kontraktfestet-sluttdato" id `arbeidsforhold kontraktfestet sluttdato`
            avhengerAv `arbeidsforhold midlertidig med kontraktfestet sluttdato`,
        dato faktum "faktum.arbeidsforhold.midlertidig-arbeidsforhold-oppstartsdato" id `arbeidsforhold midlertidig arbeidsforhold oppstartsdato`
            avhengerAv `arbeidsforhold endret`,
        boolsk faktum "faktum.arbeidsforhold.permittertert-fra-fiskeri-naering" id `arbeidsforhold permittert fra fiskeri næring`
            avhengerAv `arbeidsforhold endret`,
        periode faktum "faktum.arbeidsforhold.varighet" id `arbeidsforhold varighet`
            avhengerAv `arbeidsforhold endret`,
        boolsk faktum "faktum.arbeidsforhold.vet-du-antall-timer-foer-mistet-jobb" id `arbeidsforhold vet du antall timer før mistet jobb`
            avhengerAv `arbeidsforhold endret`,
        boolsk faktum "faktum.arbeidsforhold.vet-du-antall-timer-foer-konkurs" id `arbeidsforhold vet du antall timer før konkurs`
            avhengerAv `arbeidsforhold endret`,
        boolsk faktum "faktum.arbeidsforhold.vet-du-antall-timer-foer-kontrakt-utgikk" id `arbeidsforhold vet du antall timer før kontrakt utgikk`
            avhengerAv `arbeidsforhold endret`,
        boolsk faktum "faktum.arbeidsforhold.vet-du-antall-timer-foer-du-sa-opp" id `arbeidsforhold vet du antall timer før du sa opp`
            avhengerAv `arbeidsforhold endret`,
        boolsk faktum "faktum.arbeidsforhold.vet-du-antall-timer-foer-redusert-arbeidstid" id `arbeidsforhold vet du antall timer før redusert arbeidstid`
            avhengerAv `arbeidsforhold endret`,
        boolsk faktum "faktum.arbeidsforhold.vet-du-antall-timer-foer-permittert" id `arbeidsforhold vet du antall timer før permittert`
            avhengerAv `arbeidsforhold endret`,
        desimaltall faktum "faktum.arbeidsforhold.antall-timer-dette-arbeidsforhold" id `arbeidsforhold antall timer dette arbeidsforhold`
            avhengerAv `arbeidsforhold endret`,
        periode faktum "faktum.arbeidsforhold.permittert-periode" id `arbeidsforhold permittert periode`
            avhengerAv `arbeidsforhold endret`,
        heltall faktum "faktum.arbeidsforhold.permittert-prosent" id `arbeidsforhold permittert prosent`
            avhengerAv `arbeidsforhold endret`,
        boolsk faktum "faktum.arbeidsforhold.vet-du-lonnsplikt-periode" id `arbeidsforhold vet du lønnsplikt periode`
            avhengerAv `arbeidsforhold endret`,
        periode faktum "faktum.arbeidsforhold.naar-var-lonnsplikt-periode" id `arbeidsforhold når var lønnsplikt periode`
            avhengerAv `arbeidsforhold vet du lønnsplikt periode`,
        tekst faktum "faktum.arbeidsforhold.aarsak-til-du-sa-opp" id `arbeidsforhold årsak til du sa opp`
            avhengerAv `arbeidsforhold endret`,
        boolsk faktum "faktum.arbeidsforhold.tilbud-om-forlengelse-eller-annen-stilling" id `arbeidsforhold tilbud om forlengelse eller annen stilling`
            avhengerAv `arbeidsforhold endret`,
        envalg faktum "faktum.arbeidsforhold.svar-paa-forlengelse-eller-annen-stilling"
            med "svar.ja"
            med "svar.nei"
            med "svar.ikke-svart" id `arbeidsforhold svar på forlengelse eller annen stilling`
            avhengerAv `arbeidsforhold tilbud om forlengelse eller annen stilling`,
        tekst faktum "faktum.arbeidsforhold.aarsak-til-ikke-akseptert-tilbud" id `arbeidsforhold årsak til ikke akseptert tilbud`
            avhengerAv `arbeidsforhold svar på forlengelse eller annen stilling`,
        boolsk faktum "faktum.arbeidsforhold.soke-forskudd-lonnsgarantimidler" id `arbeidsforhold søke forskudd lønnsgarantimidler`
            avhengerAv `arbeidsforhold endret`,
        boolsk faktum "faktum.arbeidsforhold.soke-forskudd-lonnsgarantimidler-i-tillegg-til-dagpenger" id `arbeidsforhold søke forskudd lønnsgarantimidler i tillegg til dagpenger`
            avhengerAv `arbeidsforhold søke forskudd lønnsgarantimidler`,
        boolsk faktum "faktum.arbeidsforhold.godta-trekk-fra-nav-av-forskudd-fra-lonnsgarantimidler" id `arbeidsforhold godta trekk fra nav av forskudd fra lønnsgarantimidler`
            avhengerAv `arbeidsforhold søke forskudd lønnsgarantimidler`,
        envalg faktum "faktum.arbeidsforhold.har-sokt-om-lonnsgarantimidler"
            med "svar.nei"
            med "svar.nei-men-skal"
            med "svar.ja" id `arbeidsforhold har søkt om lønnsgarantimidler`
            avhengerAv `arbeidsforhold søke forskudd lønnsgarantimidler`,
        envalg faktum "faktum.arbeidsforhold.dekker-lonnsgarantiordningen-lonnskravet-ditt"
            med "svar.ja"
            med "svar.nei"
            med "svar.vet-ikke" id `arbeidsforhold dekker lønnsgarantiordningen lønnskravet ditt`
            avhengerAv `arbeidsforhold søke forskudd lønnsgarantimidler`,
        boolsk faktum "faktum.arbeidsforhold.utbetalt-lonn-etter-konkurs" id `arbeidsforhold utbetalt lønn etter konkurs`
            avhengerAv `arbeidsforhold søke forskudd lønnsgarantimidler`,
        dato faktum "faktum.arbeidsforhold.siste-dag-utbetalt-for-konkurs" id `arbeidsforhold siste dag utbetalt for konkurs`
            avhengerAv `arbeidsforhold utbetalt lønn etter konkurs`,
        tekst faktum "faktum.arbeidsforhold.hva-er-aarsak-til-avskjediget" id `arbeidsforhold hva er årsak til avskjediget`
            avhengerAv `arbeidsforhold endret`,
        tekst faktum "faktum.arbeidsforhold.vet-du-aarsak-til-sagt-opp-av-arbeidsgiver" id `arbeidsforhold vet du årsak til sagt opp av arbeidsgiver`
            avhengerAv `arbeidsforhold endret`,
        tekst faktum "faktum.arbeidsforhold.vet-du-aarsak-til-redusert-arbeidstid" id `arbeidsforhold vet du årsak til redusert arbeidstid`
            avhengerAv `arbeidsforhold endret`,
        envalg faktum "faktum.arbeidsforhold.midlertidig-arbeidsforhold-med-sluttdato"
            med "svar.ja"
            med "svar.nei"
            med "svar.vet-ikke" id `arbeidsforhold midlertidig arbeidsforhold med sluttdato`
            avhengerAv `arbeidsforhold endret`,
        boolsk faktum "faktum.arbeidsforhold.tilbud-om-annen-stilling-eller-annet-sted-i-norge" id `arbeidsforhold tilbud om annen stilling eller annet sted i norge`
            avhengerAv `arbeidsforhold endret`,
        boolsk faktum "faktum.arbeidsforhold.skift-eller-turnus" id `arbeidsforhold skift eller turnus`
            avhengerAv `arbeidsforhold endret`,
        boolsk faktum "faktum.arbeidsforhold.rotasjon" id `arbeidsforhold rotasjon`
            avhengerAv `arbeidsforhold endret`,
        heltall faktum "faktum.arbeidsforhold.arbeidsdager-siste-rotasjon" id `arbeidsforhold arbeidsdager siste rotasjon`
            avhengerAv `arbeidsforhold rotasjon`,
        heltall faktum "faktum.arbeidsforhold.fridager-siste-rotasjon" id `arbeidsforhold fridager siste rotasjon`
            avhengerAv `arbeidsforhold rotasjon`,
        heltall faktum "faktum.arbeidsforhold" id arbeidsforhold
            genererer `arbeidsforhold navn bedrift`
            og `arbeidsforhold land`
            og `arbeidsforhold endret`
            og `arbeidsforhold kjent antall timer jobbet`
            og `arbeidsforhold antall timer jobbet`
            og `arbeidsforhold har tilleggsopplysninger`
            og `arbeidsforhold tilleggsopplysninger`
            og `arbeidsforhold startdato arbeidsforhold`
            og `arbeidsforhold arbeidstid redusert fra dato`
            og `arbeidsforhold midlertidig med kontraktfestet sluttdato`
            og `arbeidsforhold kontraktfestet sluttdato`
            og `arbeidsforhold midlertidig arbeidsforhold oppstartsdato`
            og `arbeidsforhold permittert fra fiskeri næring`
            og `arbeidsforhold varighet`
            og `arbeidsforhold midlertidig arbeidsforhold med sluttdato`
            og `arbeidsforhold vet du antall timer før mistet jobb`
            og `arbeidsforhold vet du antall timer før konkurs`
            og `arbeidsforhold vet du antall timer før kontrakt utgikk`
            og `arbeidsforhold vet du antall timer før du sa opp`
            og `arbeidsforhold vet du antall timer før redusert arbeidstid`
            og `arbeidsforhold vet du antall timer før permittert`
            og `arbeidsforhold antall timer dette arbeidsforhold`
            og `arbeidsforhold permittert periode`
            og `arbeidsforhold permittert prosent`
            og `arbeidsforhold vet du lønnsplikt periode`
            og `arbeidsforhold når var lønnsplikt periode`
            og `arbeidsforhold årsak til du sa opp`
            og `arbeidsforhold tilbud om forlengelse eller annen stilling`
            og `arbeidsforhold svar på forlengelse eller annen stilling`
            og `arbeidsforhold årsak til ikke akseptert tilbud`
            og `arbeidsforhold søke forskudd lønnsgarantimidler`
            og `arbeidsforhold søke forskudd lønnsgarantimidler i tillegg til dagpenger`
            og `arbeidsforhold godta trekk fra nav av forskudd fra lønnsgarantimidler`
            og `arbeidsforhold har søkt om lønnsgarantimidler`
            og `arbeidsforhold dekker lønnsgarantiordningen lønnskravet ditt`
            og `arbeidsforhold utbetalt lønn etter konkurs`
            og `arbeidsforhold siste dag utbetalt for konkurs`
            og `arbeidsforhold hva er årsak til avskjediget`
            og `arbeidsforhold vet du årsak til sagt opp av arbeidsgiver`
            og `arbeidsforhold vet du årsak til redusert arbeidstid`
            og `arbeidsforhold tilbud om annen stilling eller annet sted i norge`
            og `arbeidsforhold skift eller turnus`
            og `arbeidsforhold rotasjon`
            og `arbeidsforhold arbeidsdager siste rotasjon`
            og `arbeidsforhold fridager siste rotasjon`,
        tekst faktum "faktum.arbeidsforhold.navn-bedrift" id `arbeidsforhold navn bedrift`,
        land faktum "faktum.arbeidsforhold.land" id `arbeidsforhold land`,
        envalg faktum "faktum.arbeidsforhold.endret"
            med "svar.ikke-endret"
            med "svar.avskjediget"
            med "svar.sagt-opp-av-arbeidsgiver"
            med "svar.arbeidsgiver-konkurs"
            med "svar.kontrakt-utgaatt"
            med "svar.sagt-opp-selv"
            med "svar.redusert-arbeidstid"
            med "svar.permittert" id `arbeidsforhold endret`,
        boolsk faktum "faktum.arbeidsforhold.gjenopptak.jobbet-siden-sist" id `gjenopptak jobbet siden sist du fikk dagpenger`,
        tekst faktum "faktum.arbeidsforhold.gjenopptak.aarsak-til-stans" id `gjenopptak årsak til stans av dagpenger`,
        dato faktum "faktum.arbeidsforhold.gjenopptak.soknadsdato-gjenopptak" id `gjenopptak søknadsdato`,
        boolsk faktum "faktum.arbeidsforhold.gjenopptak.endringer-i-arbeidsforhold" id `gjenopptak endringer i arbeidsforhold siden sist`,
        boolsk faktum "faktum.arbeidsforhold.gjenopptak.onsker-ny-beregning" id `gjenopptak ønsker ny beregning av dagpenger`,
        boolsk faktum "faktum.arbeidsforhold.gjenopptak.onsker-faa-fastsatt-ny-vanlig-arbeidstid" id `gjenopptak ønsker å få fastsatt ny vanlig arbeidstid`,

        dokument faktum "faktum.dokument-arbeidsavtale" id `dokumentasjon arbeidsavtale`,
        boolsk faktum "faktum.godkjenning-dokument-arbeidsavtale" id `godkjenning dokumentasjon arbeidsavtale`
            avhengerAv `dokumentasjon arbeidsavtale`,

        dokument faktum "faktum.dokument-helt-eller-delvis-avsluttet-arbeidsforhold" id `dokumentasjon helt eller delvis avsluttet arbeidsforhold`,
        boolsk faktum "faktum.godkjenning-dokument-helt-eller-delvis-avsluttet-arbeidsforhold"
            id `godkjenning dokumentasjon helt eller delvis avsluttet arbeidsforhold`
            avhengerAv `dokumentasjon helt eller delvis avsluttet arbeidsforhold`,

        dokument faktum "faktum.dokument-timeliste-for-rotasjon" id `dokumentasjon timeliste for rotasjon`,
        boolsk faktum "faktum.godkjenning.dokument-timeliste-for-rotasjon" id `godkjenning dokumentasjon timeliste for rotasjon`
            avhengerAv `dokumentasjon timeliste for rotasjon`,

        dokument faktum "faktum.dokument-brev-fra-bobestyrer-eller-konkursforvalter" id `dokumentasjon brev fra bobestyrer eller konkursforvalter`,
        boolsk faktum "faktum.godkjenning-dokument-brev-fra-bobestyrer-eller-konkursforvalter" id `godkjenning dokumentasjon brev fra bobestyrer eller konkursforvalter`
            avhengerAv `dokumentasjon brev fra bobestyrer eller konkursforvalter`,

        dokument faktum "faktum.dokument-ny-arbeidsavtale" id `dokumentasjon ny arbeidsavtale`,
        boolsk faktum "faktum.godkjenning-dokument-ny-arbeidsavtale" id `godkjenning dokumentasjon ny arbeidsavtale`
            avhengerAv `dokumentasjon ny arbeidsavtale`,

        dokument faktum "faktum.dokument-varsel-om-permittering" id `dokumentasjon varsel om permittering`,
        boolsk faktum "faktum.godkjenning-dokument-varsel-om-permittering" id `godkjenning dokumentasjon varsel om permittering`
            avhengerAv `dokumentasjon varsel om permittering`

    )

    override fun seksjon(søknad: Søknad) = listOf(søknad.seksjon("arbeidsforhold", Rolle.søker, *spørsmålsrekkefølge()))

    override fun regeltre(søknad: Søknad): DeltreSubsumsjon = with(søknad) {
        "arbeidsforhold".deltre {
            `har mottat dagpenger siste 12 mnd`() hvisOppfylt {
                `arbeidsforhold gjenopptak`()
            } hvisIkkeOppfylt {
                arbeidsforhold()
            }
        }
    }

    private fun Søknad.`har mottat dagpenger siste 12 mnd`() =
        envalg(Gjenopptak.`mottatt dagpenger siste 12 mnd`) inneholder Envalg("faktum.mottatt-dagpenger-siste-12-mnd.svar.ja")

    private fun Søknad.`arbeidsforhold gjenopptak`() =
        "spørsmål om gjenopptaket".alle(
            boolsk(`gjenopptak jobbet siden sist du fikk dagpenger`).utfylt(),
            tekst(`gjenopptak årsak til stans av dagpenger`).utfylt(),
            dato(`gjenopptak søknadsdato`).utfylt(),
            "hatt endringer i arbeidsforhold siden sist eller ikke".minstEnAv(
                boolsk(`gjenopptak endringer i arbeidsforhold siden sist`) er false,
                boolsk(`gjenopptak endringer i arbeidsforhold siden sist`) er true hvisOppfylt {
                    "arbeidsforhold og spørsmål om beregning og fastsatt ny arbeidstid".alle(
                        `alle arbeidsforhold`(),
                        `ønsker ny beregning og fastsatt ny arbeidstid eller ikke`()
                    )
                }
            )
        )

    private fun Søknad.`alle arbeidsforhold`() =
        generator(arbeidsforhold) med "en eller flere arbeidsforhold".deltre {
            "spørsmål om arbeidsforholdet".alle(
                tekst(`arbeidsforhold navn bedrift`).utfylt(),
                land(`arbeidsforhold land`).utfylt(),
                "hvordan arbeidsforholdet har endret seg".bareEnAv(
                    `ikke endret`(),
                    avskjediget(),
                    `sagt opp av arbeidsgiver`(),
                    `arbeidsgiver er konkurs`(),
                    `kontrakten er utgått`(),
                    `sagt opp selv`(),
                    `redusert arbeidstid`(),
                    permittert()
                )
            )
        }

    private fun Søknad.`ønsker ny beregning og fastsatt ny arbeidstid eller ikke`() =
        "ønsker ny beregning av dagpenger eller ikke".minstEnAv(
            boolsk(`gjenopptak ønsker ny beregning av dagpenger`) er false,
            boolsk(`gjenopptak ønsker ny beregning av dagpenger`) er true hvisOppfylt {
                "ønsker å få fastsatt ny vanlig arbeidstid eller ikke".minstEnAv(
                    boolsk(`gjenopptak ønsker å få fastsatt ny vanlig arbeidstid`) er false,
                    boolsk(`gjenopptak ønsker å få fastsatt ny vanlig arbeidstid`) er true hvisOppfylt {
                        `type arbeidstid`()
                    }
                )
            }
        )

    private fun Søknad.`type arbeidstid`() = envalg(`type arbeidstid`).utfylt()

    private fun Søknad.arbeidsforhold() =
        "søknadsdato, type arbeidstid og alle arbeidsforhold".alle(
            søknadsdato(),
            `type arbeidstid`(),
            envalg(`type arbeidstid`) inneholder Envalg("faktum.type-arbeidstid.svar.ingen-passer") hvisIkkeOppfylt {
                `alle arbeidsforhold`()
            }
        )

    private fun Søknad.søknadsdato() = dato(`dagpenger søknadsdato`).utfylt()

    private fun Søknad.`ikke endret`() =
        envalg(`arbeidsforhold endret`) inneholder Envalg("faktum.arbeidsforhold.endret.svar.ikke-endret") hvisOppfylt {
            "spørsmål om arbeidsforholdet".alle(
                `antall arbeidstimer ved ikke endret arbeidsforhold`(),
                "har tilleggsopplysninger eller ikke".minstEnAv(
                    boolsk(`arbeidsforhold har tilleggsopplysninger`) er false,
                    boolsk(`arbeidsforhold har tilleggsopplysninger`) er true hvisOppfylt {
                        tekst(`arbeidsforhold tilleggsopplysninger`).utfylt()
                    }
                )
            )
        }

    private fun Søknad.`antall arbeidstimer ved ikke endret arbeidsforhold`() =
        "antall arbeidstimer kjent eller ikke".minstEnAv(
            boolsk(`arbeidsforhold kjent antall timer jobbet`) er false,
            boolsk(`arbeidsforhold kjent antall timer jobbet`) er true hvisOppfylt {
                desimaltall(`arbeidsforhold antall timer jobbet`).utfylt()
            }
        )

    private fun Søknad.avskjediget() =
        (envalg(`arbeidsforhold endret`) inneholder Envalg("faktum.arbeidsforhold.endret.svar.avskjediget"))
            .sannsynliggjøresAv(
                dokument(`dokumentasjon arbeidsavtale`),
                dokument(`dokumentasjon helt eller delvis avsluttet arbeidsforhold`)
            ).godkjentAv(
                boolsk(`godkjenning dokumentasjon arbeidsavtale`),
                boolsk(`godkjenning dokumentasjon helt eller delvis avsluttet arbeidsforhold`)
            ) hvisOppfylt {
            "spørsmål om arbeidsforholdet".alle(
                `arbeidstimer før mistet jobb`(),
                tekst(`arbeidsforhold hva er årsak til avskjediget`).utfylt()
            )
        }

    private fun Søknad.`arbeidstimer før mistet jobb`() =
        "antall arbeidstimer før mistet jobb kjent eller ikke".minstEnAv(
            boolsk(`arbeidsforhold vet du antall timer før mistet jobb`) er false,
            boolsk(`arbeidsforhold vet du antall timer før mistet jobb`) er true hvisOppfylt {
                `antall timer jobbet`()
            }
        )

    private fun Søknad.`antall timer jobbet`() =
        desimaltall(`arbeidsforhold antall timer dette arbeidsforhold`).utfylt()

    private fun Søknad.`sagt opp av arbeidsgiver`() =
        (envalg(`arbeidsforhold endret`) inneholder Envalg("faktum.arbeidsforhold.endret.svar.sagt-opp-av-arbeidsgiver"))
            .sannsynliggjøresAv(
                dokument(`dokumentasjon arbeidsavtale`),
                dokument(`dokumentasjon helt eller delvis avsluttet arbeidsforhold`)
            ).godkjentAv(
                boolsk(`godkjenning dokumentasjon arbeidsavtale`),
                boolsk(`godkjenning dokumentasjon helt eller delvis avsluttet arbeidsforhold`)
            ) hvisOppfylt {
            "spørsmål om arbeidsforholdet".alle(
                `varighet på arbeidsforholdet`(),
                `arbeidstimer før mistet jobb`(),
                tekst(`arbeidsforhold vet du årsak til sagt opp av arbeidsgiver`).utfylt(),
                `tilbud om annen stilling eller annet sted i Norge`(),
                `skift, turnus og rotasjon`()
            )
        }

    private fun Søknad.`varighet på arbeidsforholdet`() =
        periode(`arbeidsforhold varighet`).utfylt()

    private fun Søknad.`tilbud om annen stilling eller annet sted i Norge`() =
        boolsk(`arbeidsforhold tilbud om annen stilling eller annet sted i norge`).utfylt()

    private fun Søknad.`skift, turnus og rotasjon`() =
        "spørsmål om skift, turnus og rotasjon".alle(
            boolsk(`arbeidsforhold skift eller turnus`).utfylt(),
            "rotasjon eller ikke".minstEnAv(
                boolsk(`arbeidsforhold rotasjon`) er false,
                (boolsk(`arbeidsforhold rotasjon`) er true)
                    .sannsynliggjøresAv(dokument(`dokumentasjon timeliste for rotasjon`))
                    .godkjentAv(
                        boolsk(`godkjenning dokumentasjon timeliste for rotasjon`)
                    ) hvisOppfylt {
                    "oppfølgingsspørsmål om rotasjonen".alle(
                        heltall(`arbeidsforhold arbeidsdager siste rotasjon`).utfylt(),
                        heltall(`arbeidsforhold fridager siste rotasjon`).utfylt()
                    )
                }
            )
        )

    private fun Søknad.`arbeidsgiver er konkurs`() =
        (envalg(`arbeidsforhold endret`) inneholder Envalg("faktum.arbeidsforhold.endret.svar.arbeidsgiver-konkurs"))
            .sannsynliggjøresAv(
                dokument(`dokumentasjon arbeidsavtale`),
                dokument(`dokumentasjon brev fra bobestyrer eller konkursforvalter`)
            ).godkjentAv(
                boolsk(`godkjenning dokumentasjon arbeidsavtale`),
                boolsk(`godkjenning dokumentasjon brev fra bobestyrer eller konkursforvalter`)
            ) hvisOppfylt {
            "spørsmål om arbeidsforholdet".alle(
                `varighet på arbeidsforholdet`(),
                envalg(`arbeidsforhold midlertidig arbeidsforhold med sluttdato`).utfylt(),
                `arbeidstimer før konkurs`(),
                lønnsgarantimidler(),
                `skift, turnus og rotasjon`()
            )
        }

    private fun Søknad.`arbeidstimer før konkurs`() =
        "antall arbeidstimer før konkurs kjent eller ikke".minstEnAv(
            boolsk(`arbeidsforhold vet du antall timer før konkurs`) er false,
            boolsk(`arbeidsforhold vet du antall timer før konkurs`) er true hvisOppfylt {
                `antall timer jobbet`()
            }
        )

    private fun Søknad.lønnsgarantimidler() =
        "ønsker å søke om forskudd på lønnsgarantimidler eller ikke".minstEnAv(
            boolsk(`arbeidsforhold søke forskudd lønnsgarantimidler`) er false,
            boolsk(`arbeidsforhold søke forskudd lønnsgarantimidler`) er true hvisOppfylt {
                `oppfølgingsspørsmål om lønnsgarantimidler`()
            }
        )

    private fun Søknad.`oppfølgingsspørsmål om lønnsgarantimidler`() =
        "spørsmål om lønnsgarantimidler".alle(
            boolsk(`arbeidsforhold søke forskudd lønnsgarantimidler i tillegg til dagpenger`).utfylt(),
            boolsk(`arbeidsforhold godta trekk fra nav av forskudd fra lønnsgarantimidler`).utfylt(),
            envalg(`arbeidsforhold har søkt om lønnsgarantimidler`).utfylt(),
            envalg(`arbeidsforhold dekker lønnsgarantiordningen lønnskravet ditt`).utfylt(),
            "fått utbetalt lønn etter konkurs eller ikke".minstEnAv(
                boolsk(`arbeidsforhold utbetalt lønn etter konkurs`) er false,
                boolsk(`arbeidsforhold utbetalt lønn etter konkurs`) er true hvisOppfylt {
                    dato(`arbeidsforhold siste dag utbetalt for konkurs`).utfylt()
                }
            )
        )

    private fun Søknad.`kontrakten er utgått`() =
        (envalg(`arbeidsforhold endret`) inneholder Envalg("faktum.arbeidsforhold.endret.svar.kontrakt-utgaatt"))
            .sannsynliggjøresAv(dokument(`dokumentasjon arbeidsavtale`))
            .godkjentAv(
                boolsk(`godkjenning dokumentasjon arbeidsavtale`)
            ) hvisOppfylt {
            "spørsmål om arbeidsforholdet".alle(
                `varighet på arbeidsforholdet`(),
                `arbeidstimer før utgått kontrakt`(),
                `tilbud om forlengelse eller annen stilling`(),
                `skift, turnus og rotasjon`()
            )
        }

    private fun Søknad.`arbeidstimer før utgått kontrakt`() =
        "antall arbeidstimer før kontrakten utgikk kjent eller ikke".minstEnAv(
            boolsk(`arbeidsforhold vet du antall timer før kontrakt utgikk`) er false,
            boolsk(`arbeidsforhold vet du antall timer før kontrakt utgikk`) er true hvisOppfylt {
                `antall timer jobbet`()
            }
        )

    private fun Søknad.`tilbud om forlengelse eller annen stilling`() =
        "tilbud om forlengelse av kontrakt eller annen stilling eller ikke".minstEnAv(
            boolsk(`arbeidsforhold tilbud om forlengelse eller annen stilling`) er false,
            boolsk(`arbeidsforhold tilbud om forlengelse eller annen stilling`) er true hvisOppfylt {
                "svar på tilbud om forlengelse eller annen stilling".minstEnAv(
                    (envalg(`arbeidsforhold svar på forlengelse eller annen stilling`) inneholder Envalg("faktum.arbeidsforhold.svar-paa-forlengelse-eller-annen-stilling.svar.ja"))
                        .sannsynliggjøresAv(dokument(`dokumentasjon ny arbeidsavtale`))
                        .godkjentAv(boolsk(`godkjenning dokumentasjon ny arbeidsavtale`)),
                    (envalg(`arbeidsforhold svar på forlengelse eller annen stilling`) inneholder Envalg("faktum.arbeidsforhold.svar-paa-forlengelse-eller-annen-stilling.svar.nei"))
                        .sannsynliggjøresAv(dokument(`dokumentasjon helt eller delvis avsluttet arbeidsforhold`))
                        .godkjentAv(
                            boolsk(`godkjenning dokumentasjon helt eller delvis avsluttet arbeidsforhold`)
                        ) hvisOppfylt {
                        tekst(`arbeidsforhold årsak til ikke akseptert tilbud`).utfylt()
                    },
                    envalg(`arbeidsforhold svar på forlengelse eller annen stilling`) inneholder Envalg("faktum.arbeidsforhold.svar-paa-forlengelse-eller-annen-stilling.svar.ikke-svart")
                )
            }
        )

    private fun Søknad.`sagt opp selv`() =
        (envalg(`arbeidsforhold endret`) inneholder Envalg("faktum.arbeidsforhold.endret.svar.sagt-opp-selv"))
            .sannsynliggjøresAv(
                dokument(`dokumentasjon arbeidsavtale`),
                dokument(`dokumentasjon helt eller delvis avsluttet arbeidsforhold`)
            )
            .godkjentAv(
                boolsk(`godkjenning dokumentasjon arbeidsavtale`),
                boolsk(`godkjenning dokumentasjon helt eller delvis avsluttet arbeidsforhold`)
            ) hvisOppfylt {
            "spørsmål om arbeidsforholdet".alle(
                `varighet på arbeidsforholdet`(),
                `arbeidstimer før sagt opp selv`(),
                tekst(`arbeidsforhold årsak til du sa opp`).utfylt(),
                `skift, turnus og rotasjon`()
            )
        }

    private fun Søknad.`arbeidstimer før sagt opp selv`() =
        "antall arbeidstimer før sagt opp selv kjent eller ikke".minstEnAv(
            boolsk(`arbeidsforhold vet du antall timer før du sa opp`) er false,
            boolsk(`arbeidsforhold vet du antall timer før du sa opp`) er true hvisOppfylt {
                `antall timer jobbet`()
            }
        )

    private fun Søknad.`redusert arbeidstid`() =
        (envalg(`arbeidsforhold endret`) inneholder Envalg("faktum.arbeidsforhold.endret.svar.redusert-arbeidstid"))
            .sannsynliggjøresAv(
                dokument(`dokumentasjon arbeidsavtale`),
                dokument(`dokumentasjon helt eller delvis avsluttet arbeidsforhold`)
            )
            .godkjentAv(
                boolsk(`godkjenning dokumentasjon arbeidsavtale`),
                boolsk(`godkjenning dokumentasjon helt eller delvis avsluttet arbeidsforhold`)
            ) hvisOppfylt {
            "spørsmål om arbeidsforholdet".alle(
                dato(`arbeidsforhold startdato arbeidsforhold`).utfylt(),
                dato(`arbeidsforhold arbeidstid redusert fra dato`).utfylt(),
                `arbeidstimer før redusert arbeidstid`(),
                tekst(`arbeidsforhold vet du årsak til redusert arbeidstid`).utfylt(),
                `tilbud om annen stilling eller annet sted i Norge`(),
                `skift, turnus og rotasjon`()
            )
        }

    private fun Søknad.`arbeidstimer før redusert arbeidstid`() =
        "antall arbeidstimer før arbedstiden ble redusert kjent eller ikke".minstEnAv(
            boolsk(`arbeidsforhold vet du antall timer før redusert arbeidstid`) er false,
            boolsk(`arbeidsforhold vet du antall timer før redusert arbeidstid`) er true hvisOppfylt {
                `antall timer jobbet`()
            }
        )

    private fun Søknad.permittert() =
        (envalg(`arbeidsforhold endret`) inneholder Envalg("faktum.arbeidsforhold.endret.svar.permittert"))
            .sannsynliggjøresAv(dokument(`dokumentasjon varsel om permittering`))
            .godkjentAv(
                boolsk(`godkjenning dokumentasjon varsel om permittering`)
            ) hvisOppfylt {
            "spørsmål om arbeidsforholdet".alle(
                `midlertidig arbeidsforhold med sluttdato`(),
                dato(`arbeidsforhold midlertidig arbeidsforhold oppstartsdato`).utfylt(),
                boolsk(`arbeidsforhold permittert fra fiskeri næring`).utfylt(),
                `arbeidstimer før permittert`(),
                periode(`arbeidsforhold permittert periode`).utfylt(),
                heltall(`arbeidsforhold permittert prosent`).utfylt(),
                lønnspliktsperiode(),
                `skift, turnus og rotasjon`()
            )
        }

    private fun Søknad.`midlertidig arbeidsforhold med sluttdato`() =
        "midlertidig arbeidsforhold med sluttdato eller ikke".minstEnAv(
            envalg(`arbeidsforhold midlertidig med kontraktfestet sluttdato`) inneholder Envalg("faktum.arbeidsforhold.midlertidig-med-kontraktfestet-sluttdato.svar.ja") hvisOppfylt {
                dato(`arbeidsforhold kontraktfestet sluttdato`).utfylt()
            },
            envalg(`arbeidsforhold midlertidig med kontraktfestet sluttdato`).utfylt()
        )

    private fun Søknad.`arbeidstimer før permittert`() =
        "antall arbeidstimer før permittert kjent eller ikke".minstEnAv(
            boolsk(`arbeidsforhold vet du antall timer før permittert`) er false,
            boolsk(`arbeidsforhold vet du antall timer før permittert`) er true hvisOppfylt {
                `antall timer jobbet`()
            }
        )

    private fun Søknad.lønnspliktsperiode() =
        "vet hva lønnspliktsperioden er eller ikke".minstEnAv(
            boolsk(`arbeidsforhold vet du lønnsplikt periode`) er false,
            boolsk(`arbeidsforhold vet du lønnsplikt periode`) er true hvisOppfylt {
                periode(`arbeidsforhold når var lønnsplikt periode`).utfylt()
            }
        )

    override val spørsmålsrekkefølge = listOf(
        `dagpenger søknadsdato`,
        `type arbeidstid`,
        arbeidsforhold,
        `arbeidsforhold navn bedrift`,
        `arbeidsforhold land`,
        `arbeidsforhold endret`,
        `arbeidsforhold kjent antall timer jobbet`,
        `arbeidsforhold antall timer jobbet`,
        `arbeidsforhold tilleggsopplysninger`,
        `arbeidsforhold startdato arbeidsforhold`,
        `arbeidsforhold arbeidstid redusert fra dato`,
        `arbeidsforhold midlertidig med kontraktfestet sluttdato`,
        `arbeidsforhold kontraktfestet sluttdato`,
        `arbeidsforhold midlertidig arbeidsforhold oppstartsdato`,
        `arbeidsforhold permittert fra fiskeri næring`,
        `arbeidsforhold varighet`,
        `arbeidsforhold vet du antall timer før mistet jobb`,
        `arbeidsforhold vet du antall timer før konkurs`,
        `arbeidsforhold vet du antall timer før kontrakt utgikk`,
        `arbeidsforhold vet du antall timer før du sa opp`,
        `arbeidsforhold vet du antall timer før redusert arbeidstid`,
        `arbeidsforhold vet du antall timer før permittert`,
        `arbeidsforhold antall timer dette arbeidsforhold`,
        `arbeidsforhold permittert periode`,
        `arbeidsforhold permittert prosent`,
        `arbeidsforhold vet du lønnsplikt periode`,
        `arbeidsforhold når var lønnsplikt periode`,
        `arbeidsforhold årsak til du sa opp`,
        `arbeidsforhold tilbud om forlengelse eller annen stilling`,
        `arbeidsforhold svar på forlengelse eller annen stilling`,
        `arbeidsforhold årsak til ikke akseptert tilbud`,
        `arbeidsforhold søke forskudd lønnsgarantimidler`,
        `arbeidsforhold søke forskudd lønnsgarantimidler i tillegg til dagpenger`,
        `arbeidsforhold godta trekk fra nav av forskudd fra lønnsgarantimidler`,
        `arbeidsforhold har søkt om lønnsgarantimidler`,
        `arbeidsforhold dekker lønnsgarantiordningen lønnskravet ditt`,
        `arbeidsforhold utbetalt lønn etter konkurs`,
        `arbeidsforhold siste dag utbetalt for konkurs`,
        `arbeidsforhold hva er årsak til avskjediget`,
        `arbeidsforhold vet du årsak til sagt opp av arbeidsgiver`,
        `arbeidsforhold vet du årsak til redusert arbeidstid`,
        `arbeidsforhold midlertidig arbeidsforhold med sluttdato`,
        `arbeidsforhold tilbud om annen stilling eller annet sted i norge`,
        `arbeidsforhold skift eller turnus`,
        `arbeidsforhold rotasjon`,
        `arbeidsforhold arbeidsdager siste rotasjon`,
        `arbeidsforhold fridager siste rotasjon`,
        `arbeidsforhold har tilleggsopplysninger`,
        `gjenopptak jobbet siden sist du fikk dagpenger`,
        `gjenopptak årsak til stans av dagpenger`,
        `gjenopptak søknadsdato`,
        `gjenopptak endringer i arbeidsforhold siden sist`,
        `gjenopptak ønsker ny beregning av dagpenger`,
        `gjenopptak ønsker å få fastsatt ny vanlig arbeidstid`,

        `dokumentasjon arbeidsavtale`,
        `godkjenning dokumentasjon arbeidsavtale`,
        `dokumentasjon helt eller delvis avsluttet arbeidsforhold`,
        `godkjenning dokumentasjon helt eller delvis avsluttet arbeidsforhold`,
        `dokumentasjon timeliste for rotasjon`,
        `godkjenning dokumentasjon timeliste for rotasjon`,
        `dokumentasjon brev fra bobestyrer eller konkursforvalter`,
        `godkjenning dokumentasjon brev fra bobestyrer eller konkursforvalter`,
        `dokumentasjon ny arbeidsavtale`,
        `godkjenning dokumentasjon ny arbeidsavtale`,
        `dokumentasjon varsel om permittering`,
        `godkjenning dokumentasjon varsel om permittering`
    )
}
