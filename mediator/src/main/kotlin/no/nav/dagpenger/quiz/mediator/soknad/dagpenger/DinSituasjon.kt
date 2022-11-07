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

object DinSituasjon : DslFaktaseksjon {
    const val `mottatt dagpenger siste 12 mnd` = 8001
    const val `gjenopptak jobbet siden sist du fikk dagpenger` = 8002
    const val `gjenopptak årsak til stans av dagpenger` = 8003
    const val `gjenopptak søknadsdato` = 8004
    const val `gjenopptak endringer i arbeidsforhold siden sist` = 8005
    const val `gjenopptak ønsker ny beregning av dagpenger` = 8006
    const val `gjenopptak ønsker å få fastsatt ny vanlig arbeidstid` = 8007
    const val `type arbeidstid` = 8008
    const val `dagpenger søknadsdato` = 8009
    const val arbeidsforhold = 8010
    const val `arbeidsforhold navn bedrift` = 8011
    const val `arbeidsforhold land` = 8012
    const val `arbeidsforhold endret` = 8013
    const val `arbeidsforhold kjent antall timer jobbet` = 8014
    const val `arbeidsforhold antall timer jobbet` = 8015
    const val `arbeidsforhold har tilleggsopplysninger` = 8016
    const val `arbeidsforhold tilleggsopplysninger` = 8017
    const val `arbeidsforhold startdato arbeidsforhold` = 8018
    const val `arbeidsforhold arbeidstid redusert fra dato` = 8019
    const val `arbeidsforhold midlertidig med kontraktfestet sluttdato` = 8020
    const val `arbeidsforhold kontraktfestet sluttdato` = 8021
    const val `arbeidsforhold midlertidig arbeidsforhold oppstartsdato` = 8022
    const val `arbeidsforhold permittert fra fiskeri næring` = 8023
    const val `arbeidsforhold varighet` = 8024
    const val `arbeidsforhold vet du antall timer før mistet jobb` = 8025
    const val `arbeidsforhold vet du antall timer før konkurs` = 8026
    const val `arbeidsforhold vet du antall timer før kontrakt utgikk` = 8027
    const val `arbeidsforhold vet du antall timer før du sa opp` = 8028
    const val `arbeidsforhold vet du antall timer før redusert arbeidstid` = 8029
    const val `arbeidsforhold vet du antall timer før permittert` = 8030
    const val `arbeidsforhold antall timer dette arbeidsforhold` = 8031
    const val `arbeidsforhold permittert periode` = 8032
    const val `arbeidsforhold permittert prosent` = 8033
    const val `arbeidsforhold vet du lønnsplikt periode` = 8034
    const val `arbeidsforhold når var lønnsplikt periode` = 8035
    const val `arbeidsforhold årsak til du sa opp` = 8036
    const val `arbeidsforhold tilbud om forlengelse eller annen stilling` = 8037
    const val `arbeidsforhold svar på forlengelse eller annen stilling` = 8038
    const val `arbeidsforhold årsak til ikke akseptert tilbud` = 8039
    const val `arbeidsforhold søke forskudd lønnsgarantimidler` = 8040
    const val `arbeidsforhold søke forskudd lønnsgarantimidler i tillegg til dagpenger` = 8041
    const val `arbeidsforhold godta trekk fra nav av forskudd fra lønnsgarantimidler` = 8042
    const val `arbeidsforhold har søkt om lønnsgarantimidler` = 8043
    const val `arbeidsforhold dekker lønnsgarantiordningen lønnskravet ditt` = 8044
    const val `arbeidsforhold utbetalt lønn etter konkurs` = 8045
    const val `arbeidsforhold siste dag utbetalt for konkurs` = 8046
    const val `arbeidsforhold hva er årsak til avskjediget` = 8047
    const val `arbeidsforhold hva er årsak til sagt opp av arbeidsgiver` = 8048
    const val `arbeidsforhold hva er årsak til redusert arbeidstid` = 8049
    const val `arbeidsforhold midlertidig arbeidsforhold med sluttdato` = 8050
    const val `arbeidsforhold tilbud om annen stilling eller annet sted i norge` = 8051
    const val `arbeidsforhold skift eller turnus` = 8052
    const val `arbeidsforhold rotasjon` = 8053
    const val `arbeidsforhold arbeidsdager siste rotasjon` = 8054
    const val `arbeidsforhold fridager siste rotasjon` = 8055
    const val arbeidsavtale = 8056
    const val `dokumentasjon av arbeidsforhold` = 8057
    const val timelister = 8058
    const val `brev fra bobestyrer eller konkursforvalter` = 8059
    const val `ny arbeidsavtale` = 8060
    const val permitteringsvarsel = 8061
    const val `godkjenning av arbeidsforhold-dokumentasjon` = 8062

    override val fakta = listOf(
        envalg faktum "faktum.mottatt-dagpenger-siste-12-mnd"
            med "svar.ja"
            med "svar.nei"
            med "svar.vet-ikke" id `mottatt dagpenger siste 12 mnd`,
        boolsk faktum "faktum.arbeidsforhold.gjenopptak.jobbet-siden-sist"
            id `gjenopptak jobbet siden sist du fikk dagpenger`
            avhengerAv `mottatt dagpenger siste 12 mnd`,
        tekst faktum "faktum.arbeidsforhold.gjenopptak.aarsak-til-stans" id `gjenopptak årsak til stans av dagpenger`
            avhengerAv `mottatt dagpenger siste 12 mnd`,
        dato faktum "faktum.arbeidsforhold.gjenopptak.soknadsdato-gjenopptak"
            id `gjenopptak søknadsdato` avhengerAv `mottatt dagpenger siste 12 mnd`,
        boolsk faktum "faktum.arbeidsforhold.gjenopptak.endringer-i-arbeidsforhold"
            id `gjenopptak endringer i arbeidsforhold siden sist`
            avhengerAv `mottatt dagpenger siste 12 mnd`,
        boolsk faktum "faktum.arbeidsforhold.gjenopptak.onsker-ny-beregning"
            id `gjenopptak ønsker ny beregning av dagpenger`
            avhengerAv `mottatt dagpenger siste 12 mnd`,
        boolsk faktum "faktum.arbeidsforhold.gjenopptak.onsker-faa-fastsatt-ny-vanlig-arbeidstid"
            id `gjenopptak ønsker å få fastsatt ny vanlig arbeidstid`
            avhengerAv `mottatt dagpenger siste 12 mnd` og `gjenopptak ønsker ny beregning av dagpenger`,
        envalg faktum "faktum.type-arbeidstid"
            med "svar.fast"
            med "svar.varierende"
            med "svar.kombinasjon"
            med "svar.ingen-passer" id `type arbeidstid` avhengerAv `mottatt dagpenger siste 12 mnd`,
        dato faktum "faktum.dagpenger-soknadsdato" id `dagpenger søknadsdato` avhengerAv `mottatt dagpenger siste 12 mnd`,
        heltall faktum "faktum.arbeidsforhold" id arbeidsforhold
            genererer `arbeidsforhold navn bedrift`
            navngittAv `arbeidsforhold navn bedrift`
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
            og `arbeidsforhold hva er årsak til sagt opp av arbeidsgiver`
            og `arbeidsforhold hva er årsak til redusert arbeidstid`
            og `arbeidsforhold tilbud om annen stilling eller annet sted i norge`
            og `arbeidsforhold skift eller turnus`
            og `arbeidsforhold rotasjon`
            og `arbeidsforhold arbeidsdager siste rotasjon`
            og `arbeidsforhold fridager siste rotasjon`
            og arbeidsavtale
            og `dokumentasjon av arbeidsforhold`
            og `brev fra bobestyrer eller konkursforvalter`
            og permitteringsvarsel
            og timelister
            og `ny arbeidsavtale`,
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
        boolsk faktum "faktum.arbeidsforhold.kjent-antall-timer-jobbet" id `arbeidsforhold kjent antall timer jobbet`
            avhengerAv `arbeidsforhold endret`,
        desimaltall faktum "faktum.arbeidsforhold.antall-timer-jobbet" id `arbeidsforhold antall timer jobbet`
            avhengerAv `arbeidsforhold kjent antall timer jobbet`,
        boolsk faktum "faktum.arbeidsforhold.har-tilleggsopplysninger"
            id `arbeidsforhold har tilleggsopplysninger`
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
        dato faktum "faktum.arbeidsforhold.midlertidig-arbeidsforhold-oppstartsdato"
            id `arbeidsforhold midlertidig arbeidsforhold oppstartsdato`
            avhengerAv `arbeidsforhold endret`,
        boolsk faktum "faktum.arbeidsforhold.permittertert-fra-fiskeri-naering"
            id `arbeidsforhold permittert fra fiskeri næring`
            avhengerAv `arbeidsforhold endret`,
        periode faktum "faktum.arbeidsforhold.varighet" id `arbeidsforhold varighet`
            avhengerAv `arbeidsforhold endret`,
        boolsk faktum "faktum.arbeidsforhold.vet-du-antall-timer-foer-mistet-jobb"
            id `arbeidsforhold vet du antall timer før mistet jobb`
            avhengerAv `arbeidsforhold endret`,
        boolsk faktum "faktum.arbeidsforhold.vet-du-antall-timer-foer-konkurs"
            id `arbeidsforhold vet du antall timer før konkurs`
            avhengerAv `arbeidsforhold endret`,
        boolsk faktum "faktum.arbeidsforhold.vet-du-antall-timer-foer-kontrakt-utgikk"
            id `arbeidsforhold vet du antall timer før kontrakt utgikk`
            avhengerAv `arbeidsforhold endret`,
        boolsk faktum "faktum.arbeidsforhold.vet-du-antall-timer-foer-du-sa-opp"
            id `arbeidsforhold vet du antall timer før du sa opp`
            avhengerAv `arbeidsforhold endret`,
        boolsk faktum "faktum.arbeidsforhold.vet-du-antall-timer-foer-redusert-arbeidstid"
            id `arbeidsforhold vet du antall timer før redusert arbeidstid`
            avhengerAv `arbeidsforhold endret`,
        boolsk faktum "faktum.arbeidsforhold.vet-du-antall-timer-foer-permittert"
            id `arbeidsforhold vet du antall timer før permittert`
            avhengerAv `arbeidsforhold endret`,
        desimaltall faktum "faktum.arbeidsforhold.antall-timer-dette-arbeidsforhold"
            id `arbeidsforhold antall timer dette arbeidsforhold`
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
        boolsk faktum "faktum.arbeidsforhold.tilbud-om-forlengelse-eller-annen-stilling"
            id `arbeidsforhold tilbud om forlengelse eller annen stilling`
            avhengerAv `arbeidsforhold endret`,
        envalg faktum "faktum.arbeidsforhold.svar-paa-forlengelse-eller-annen-stilling"
            med "svar.ja"
            med "svar.nei"
            med "svar.ikke-svart" id `arbeidsforhold svar på forlengelse eller annen stilling`
            avhengerAv `arbeidsforhold tilbud om forlengelse eller annen stilling`,
        tekst faktum "faktum.arbeidsforhold.aarsak-til-ikke-akseptert-tilbud"
            id `arbeidsforhold årsak til ikke akseptert tilbud`
            avhengerAv `arbeidsforhold svar på forlengelse eller annen stilling`,
        boolsk faktum "faktum.arbeidsforhold.soke-forskudd-lonnsgarantimidler"
            id `arbeidsforhold søke forskudd lønnsgarantimidler`
            avhengerAv `arbeidsforhold endret`,
        boolsk faktum "faktum.arbeidsforhold.soke-forskudd-lonnsgarantimidler-i-tillegg-til-dagpenger"
            id `arbeidsforhold søke forskudd lønnsgarantimidler i tillegg til dagpenger`
            avhengerAv `arbeidsforhold søke forskudd lønnsgarantimidler`,
        boolsk faktum "faktum.arbeidsforhold.godta-trekk-fra-nav-av-forskudd-fra-lonnsgarantimidler"
            id `arbeidsforhold godta trekk fra nav av forskudd fra lønnsgarantimidler`
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
        boolsk faktum "faktum.arbeidsforhold.utbetalt-lonn-etter-konkurs"
            id `arbeidsforhold utbetalt lønn etter konkurs`
            avhengerAv `arbeidsforhold søke forskudd lønnsgarantimidler`,
        dato faktum "faktum.arbeidsforhold.siste-dag-utbetalt-for-konkurs"
            id `arbeidsforhold siste dag utbetalt for konkurs`
            avhengerAv `arbeidsforhold utbetalt lønn etter konkurs`,
        tekst faktum "faktum.arbeidsforhold.hva-er-aarsak-til-avskjediget"
            id `arbeidsforhold hva er årsak til avskjediget`
            avhengerAv `arbeidsforhold endret`,
        tekst faktum "faktum.arbeidsforhold.vet-du-aarsak-til-sagt-opp-av-arbeidsgiver"
            id `arbeidsforhold hva er årsak til sagt opp av arbeidsgiver`
            avhengerAv `arbeidsforhold endret`,
        tekst faktum "faktum.arbeidsforhold.vet-du-aarsak-til-redusert-arbeidstid"
            id `arbeidsforhold hva er årsak til redusert arbeidstid`
            avhengerAv `arbeidsforhold endret`,
        envalg faktum "faktum.arbeidsforhold.midlertidig-arbeidsforhold-med-sluttdato"
            med "svar.ja"
            med "svar.nei"
            med "svar.vet-ikke" id `arbeidsforhold midlertidig arbeidsforhold med sluttdato`
            avhengerAv `arbeidsforhold endret`,
        boolsk faktum "faktum.arbeidsforhold.tilbud-om-annen-stilling-eller-annet-sted-i-norge"
            id `arbeidsforhold tilbud om annen stilling eller annet sted i norge`
            avhengerAv `arbeidsforhold endret`,
        boolsk faktum "faktum.arbeidsforhold.skift-eller-turnus" id `arbeidsforhold skift eller turnus`
            avhengerAv `arbeidsforhold endret`,
        boolsk faktum "faktum.arbeidsforhold.rotasjon" id `arbeidsforhold rotasjon`
            avhengerAv `arbeidsforhold endret`,
        heltall faktum "faktum.arbeidsforhold.arbeidsdager-siste-rotasjon"
            id `arbeidsforhold arbeidsdager siste rotasjon`
            avhengerAv `arbeidsforhold rotasjon`,
        heltall faktum "faktum.arbeidsforhold.fridager-siste-rotasjon" id `arbeidsforhold fridager siste rotasjon`
            avhengerAv `arbeidsforhold rotasjon`,
        dokument faktum "faktum.dokument-arbeidsavtale" id arbeidsavtale,
        dokument faktum "faktum.dokument-dokumentasjon-av-arbeidsforhold" id `dokumentasjon av arbeidsforhold`,
        dokument faktum "faktum.dokument-timelister" id timelister,
        dokument faktum "faktum.dokument-brev-fra-bobestyrer-eller-konkursforvalter"
            id `brev fra bobestyrer eller konkursforvalter`,
        dokument faktum "faktum.dokument-ny-arbeidsavtale" id `ny arbeidsavtale`,
        dokument faktum "faktum.dokument-permitteringsvarsel" id permitteringsvarsel,
        boolsk faktum "faktum.godkjenning-arbeidsforhold-dokumentasjon" id `godkjenning av arbeidsforhold-dokumentasjon`
            avhengerAv permitteringsvarsel
            og `ny arbeidsavtale`
            og arbeidsavtale
            og `dokumentasjon av arbeidsforhold`
            og timelister
            og `brev fra bobestyrer eller konkursforvalter`

    )

    override fun seksjon(søknad: Søknad) =
        listOf(søknad.seksjon("gjenopptak", Rolle.søker, *spørsmålsrekkefølgeForSøker()))

    override fun regeltre(søknad: Søknad): DeltreSubsumsjon = with(søknad) {
        "din situasjon".deltre {
            `har mottatt dagpenger siste tolv måneder`() hvisOppfylt {
                gjenopptak()
            } hvisIkkeOppfylt {
                `ny søknad`()
            }
        }
    }

    private fun Søknad.`har mottatt dagpenger siste tolv måneder`() =
        (envalg(`mottatt dagpenger siste 12 mnd`) inneholder Envalg("faktum.mottatt-dagpenger-siste-12-mnd.svar.ja"))

    private fun Søknad.gjenopptak() =
        "spørsmål om gjenopptaket".alle(
            tekst(`gjenopptak årsak til stans av dagpenger`).utfylt(),
            dato(`gjenopptak søknadsdato`).utfylt(),
            "jobbet siden sist eller ikke".minstEnAv(
                boolsk(`gjenopptak jobbet siden sist du fikk dagpenger`) er false,
                boolsk(`gjenopptak jobbet siden sist du fikk dagpenger`) er true hvisOppfylt {
                    "hatt endringer i arbeidsforhold siden sist eller ikke".minstEnAv(
                        boolsk(`gjenopptak endringer i arbeidsforhold siden sist`) er false,
                        boolsk(`gjenopptak endringer i arbeidsforhold siden sist`) er true hvisOppfylt {
                            `ønske om ny beregning og fastsatt ny arbeidstid eller ikke`()
                        }
                    )
                }
            )
        )

    private fun Søknad.`ønske om ny beregning og fastsatt ny arbeidstid eller ikke`() =
        "ønsker ny beregning av dagpenger eller ikke".minstEnAv(
            boolsk(`gjenopptak ønsker ny beregning av dagpenger`) er false,
            boolsk(`gjenopptak ønsker ny beregning av dagpenger`) er true hvisOppfylt {
                "ønsker å få fastsatt ny vanlig arbeidstid eller ikke".minstEnAv(
                    boolsk(`gjenopptak ønsker å få fastsatt ny vanlig arbeidstid`) er false,
                    boolsk(`gjenopptak ønsker å få fastsatt ny vanlig arbeidstid`) er true hvisOppfylt {
                        `har hatt arbeidstid`() hvisOppfylt {
                            `alle arbeidsforhold`()
                        } hvisIkkeOppfylt {
                            envalg(`type arbeidstid`) inneholder Envalg("faktum.type-arbeidstid.svar.ingen-passer")
                        }
                    }
                )
            }
        )

    private fun Søknad.`har hatt arbeidstid`() = "fast, varierende eller kombinasjon".minstEnAv(
        envalg(`type arbeidstid`) inneholder Envalg("faktum.type-arbeidstid.svar.fast"),
        envalg(`type arbeidstid`) inneholder Envalg("faktum.type-arbeidstid.svar.varierende"),
        envalg(`type arbeidstid`) inneholder Envalg("faktum.type-arbeidstid.svar.kombinasjon")
    )

    private fun Søknad.`ny søknad`() = "søknadsdato, type arbeidstid og arbeidsforhold".alle(
        dato(`dagpenger søknadsdato`).utfylt(),
        `har hatt arbeidstid`() hvisOppfylt {
            `alle arbeidsforhold`()
        } hvisIkkeOppfylt {
            envalg(`type arbeidstid`) inneholder Envalg("faktum.type-arbeidstid.svar.ingen-passer")
        }
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
                dokument(arbeidsavtale),
                dokument(`dokumentasjon av arbeidsforhold`)
            ).godkjentAv(
                boolsk(`godkjenning av arbeidsforhold-dokumentasjon`)
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
                dokument(arbeidsavtale),
                dokument(`dokumentasjon av arbeidsforhold`)
            ).godkjentAv(
                boolsk(`godkjenning av arbeidsforhold-dokumentasjon`)
            ) hvisOppfylt {
            "spørsmål om arbeidsforholdet".alle(
                `varighet på arbeidsforholdet`(),
                `arbeidstimer før mistet jobb`(),
                tekst(`arbeidsforhold hva er årsak til sagt opp av arbeidsgiver`).utfylt(),
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
                    .sannsynliggjøresAv(dokument(timelister))
                    .godkjentAv(
                        boolsk(`godkjenning av arbeidsforhold-dokumentasjon`)
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
                dokument(arbeidsavtale),
                dokument(`brev fra bobestyrer eller konkursforvalter`)
            ).godkjentAv(
                boolsk(`godkjenning av arbeidsforhold-dokumentasjon`)
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
            .sannsynliggjøresAv(dokument(arbeidsavtale))
            .godkjentAv(
                boolsk(`godkjenning av arbeidsforhold-dokumentasjon`)
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
                        .sannsynliggjøresAv(dokument(`ny arbeidsavtale`))
                        .godkjentAv(boolsk(`godkjenning av arbeidsforhold-dokumentasjon`)),
                    (envalg(`arbeidsforhold svar på forlengelse eller annen stilling`) inneholder Envalg("faktum.arbeidsforhold.svar-paa-forlengelse-eller-annen-stilling.svar.nei"))
                        .sannsynliggjøresAv(dokument(`dokumentasjon av arbeidsforhold`))
                        .godkjentAv(
                            boolsk(`godkjenning av arbeidsforhold-dokumentasjon`)
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
                dokument(arbeidsavtale),
                dokument(`dokumentasjon av arbeidsforhold`)
            )
            .godkjentAv(
                boolsk(`godkjenning av arbeidsforhold-dokumentasjon`)
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
                dokument(arbeidsavtale),
                dokument(`dokumentasjon av arbeidsforhold`)
            )
            .godkjentAv(
                boolsk(`godkjenning av arbeidsforhold-dokumentasjon`)
            ) hvisOppfylt {
            "spørsmål om arbeidsforholdet".alle(
                dato(`arbeidsforhold startdato arbeidsforhold`).utfylt(),
                dato(`arbeidsforhold arbeidstid redusert fra dato`).utfylt(),
                `arbeidstimer før redusert arbeidstid`(),
                tekst(`arbeidsforhold hva er årsak til redusert arbeidstid`).utfylt(),
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
            .sannsynliggjøresAv(dokument(permitteringsvarsel))
            .godkjentAv(
                boolsk(`godkjenning av arbeidsforhold-dokumentasjon`)
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

    override val spørsmålsrekkefølgeForSøker = listOf(
        `mottatt dagpenger siste 12 mnd`,
        `gjenopptak jobbet siden sist du fikk dagpenger`,
        `gjenopptak årsak til stans av dagpenger`,
        `gjenopptak søknadsdato`,
        `gjenopptak endringer i arbeidsforhold siden sist`,
        `gjenopptak ønsker ny beregning av dagpenger`,
        `gjenopptak ønsker å få fastsatt ny vanlig arbeidstid`,
        `type arbeidstid`,
        `dagpenger søknadsdato`,
        arbeidsforhold,
        `arbeidsforhold navn bedrift`,
        `arbeidsforhold land`,
        `arbeidsforhold endret`,
        `arbeidsforhold kjent antall timer jobbet`,
        `arbeidsforhold antall timer jobbet`,
        `arbeidsforhold har tilleggsopplysninger`,
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
        `arbeidsforhold hva er årsak til sagt opp av arbeidsgiver`,
        `arbeidsforhold hva er årsak til redusert arbeidstid`,
        `arbeidsforhold midlertidig arbeidsforhold med sluttdato`,
        `arbeidsforhold tilbud om annen stilling eller annet sted i norge`,
        `arbeidsforhold skift eller turnus`,
        `arbeidsforhold rotasjon`,
        `arbeidsforhold arbeidsdager siste rotasjon`,
        `arbeidsforhold fridager siste rotasjon`,
        arbeidsavtale,
        `dokumentasjon av arbeidsforhold`,
        timelister,
        `brev fra bobestyrer eller konkursforvalter`,
        `ny arbeidsavtale`,
        permitteringsvarsel,
        `godkjenning av arbeidsforhold-dokumentasjon`
    )
}
