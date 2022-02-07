package no.nav.dagpenger.quiz.mediator.soknad.dagpenger

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.boolsk
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dato
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.desimaltall
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.envalg
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.flervalg
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.heltall
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.periode
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.tekst
import no.nav.dagpenger.model.factory.FaktumFactory

object Arbeidsforhold {
    const val `dagpenger soknadsdato` = 8001
    const val `fast arbeidstid` = 8002
    const val arbeidsforhold = 8003
    const val `navn bedrift` = 8004
    const val `arbeidsforhold land` = 8005
    const val `arbeidsforhold aarsak` = 8006
    const val `arbeidsforhold varighet` = 8007
    const val `arbeidsforhold ekstra opplysninger laerlig` = 8008
    const val `arbeidsforhold ekstra opplysninger fiskeindustri` = 8009
    const val `arbeidsforhold ekstra opplysninger flere arbeidsforhold` = 8010
    const val `arbeidsforhold arbeidstid timer i uken alle forhold` = 8011
    const val `arbeidsforhold arbeidstid timer i uken` = 8012
    const val `arbeidsforhold aarsak til oppsigelse fra arbeidsgiver` = 8013
    const val `arbeidsforhold aarsak til avskjedigelse fra arbeidsgiver` = 8014
    const val `tilbud annen stilling annet sted samme arbeidsgiver` = 8015
    const val `tilbud forsette samme arbeidsgiver` = 8016
    const val `arbeids skift turnus rotasjon` = 8017
    const val `arbeidsforhold rotasjon antall arbeidsdager` = 8018
    const val `arbeidsforhold rotasjon antall fridager` = 8019
    const val `midlertidig arbeidsforhold med sluttdato` = 8020
    const val `midlertidig arbeidsforhold sluttdato` = 8021
    const val `arbeidsforhold permitert` = 8022
    const val `arbeidsforhold permitteringsperiode` = 8023
    const val `arbeidsforhold permitteringgrad` = 8024
    const val `arbeidsforhold lonnsplinkt arbeidsgiver` = 8025
    const val `aarsak til sagt opp selv` = 8026
    const val `arbeidsforhold arbeidsgiver konkurs navn bostyrer` = 8027
    const val `arbeidsforhold dagpenger og forskudd lonnsgarantimidler` = 8028
    const val `arbeidsforhold godta nav trekk direkte lonnsgaranti` = 8029
    const val `arbeidsforhold sok lonnsgarantimidler` = 8030
    const val `arbeidsforhold lonnsgaranti dekker krav` = 8031
    const val `arbeidsforhold godta trekk direkte konkursbo` = 8032
    const val `arbeidsforhold utbetalt lonn etter konkurs` = 8033
    const val `faktum arbeidsforhold konkurs siste dag lonn` = 8034
    const val `arbeidsforhold tillegsinformasjon` = 8035

    private val fakta = listOf<FaktumFactory<*>>(
        dato faktum "faktum.dagpenger-soknadsdato" id `dagpenger soknadsdato`,
        envalg faktum "faktum.fast-arbeidstid"
            med "svar.ja-fast"
            med "svar.nei-varierende"
            med "svar.kombinasjon"
            med "svar.ingen-passer" id `fast arbeidstid`,
        periode faktum "faktum.arbeidsforhold-varighet" id `arbeidsforhold varighet`,
        flervalg faktum "faktum.arbeidsforhold-ekstra-opplysninger-laerlig"
            med "faktum.arbeidsforhold-ekstra-opplysninger.svar.laerling" id `arbeidsforhold ekstra opplysninger laerlig`,
        flervalg faktum "faktum.arbeidsforhold-ekstra-opplysninger-fiskeindustri"
            med "faktum.arbeidsforhold-ekstra-opplysninger.svar.fiskeindustri" id `arbeidsforhold ekstra opplysninger fiskeindustri`,
        flervalg faktum "faktum.arbeidsforhold-ekstra-opplysninger-flere-arbeidsforhold"
            med "faktum.arbeidsforhold-ekstra-opplysninger.svar.flere-arbeidsforhold" id `arbeidsforhold ekstra opplysninger flere arbeidsforhold`,
        desimaltall faktum "faktum.arbeidsforhold-arbeidstid-timer-i-uken-alle-forhold" id `arbeidsforhold arbeidstid timer i uken alle forhold`,
        desimaltall faktum "faktum.arbeidsforhold-arbeidstid-timer-i-uken" id `arbeidsforhold arbeidstid timer i uken`,
        tekst faktum "faktum.arbeidsforhold-aarsak-til-oppsigelse-fra-arbeidsgiver" id `arbeidsforhold aarsak til oppsigelse fra arbeidsgiver`,
        tekst faktum "faktum.arbeidsforhold-aarsak-til-avskjedigelse-fra-arbeidsgiver" id `arbeidsforhold aarsak til avskjedigelse fra arbeidsgiver`,
        boolsk faktum "faktum.tilbud-annen-stilling-annet-sted-samme-arbeidsgiver" id `tilbud annen stilling annet sted samme arbeidsgiver`,
        boolsk faktum "faktum.tilbud-forsette-samme-arbeidsgiver" id `tilbud forsette samme arbeidsgiver`,
        envalg faktum "faktum.arbeids-skift-turnus-rotasjon"
            med "svar.nei"
            med "svar.ja-skift-turnus"
            med "svar.ja-rotasjon" id `arbeids skift turnus rotasjon`,
        heltall faktum "faktum.arbeidsforhold-rotasjon-antall-arbeidsdager" id `arbeidsforhold rotasjon antall arbeidsdager`,
        heltall faktum "faktum.arbeidsforhold-rotasjon-antall-fridager" id `arbeidsforhold rotasjon antall fridager`,
        envalg faktum "faktum.midlertidig-arbeidsforhold-med-sluttdato"
            med "svar.nei-fast-arbeid"
            med "svar.ja"
            med "svar.vet-ikke" id `midlertidig arbeidsforhold med sluttdato`,
        dato faktum "faktum.midlertidig-arbeidsforhold-sluttdato" id `midlertidig arbeidsforhold sluttdato`,
        heltall faktum "faktum.arbeidsforhold-permitert" id `arbeidsforhold permitert`
            genererer `arbeidsforhold permitteringsperiode`
            og `arbeidsforhold permitteringgrad`,
        periode faktum "faktum.arbeidsforhold-permitteringsperiode" id `arbeidsforhold permitteringsperiode`,
        heltall faktum "faktum.arbeidsforhold-permitteringgrad" id `arbeidsforhold permitteringgrad`,
        periode faktum "faktum.arbeidsforhold-lonnsplinkt-arbeidsgiver" id `arbeidsforhold lonnsplinkt arbeidsgiver`,
        tekst faktum "faktum.aarsak-til-sagt-opp-selv" id `aarsak til sagt opp selv`,
        tekst faktum "faktum.arbeidsforhold-arbeidsgiver-konkurs-navn-bostyrer" id `arbeidsforhold arbeidsgiver konkurs navn bostyrer`,
        envalg faktum "faktum.arbeidsforhold-dagpenger-og-forskudd-lonnsgarantimidler"
            med "svar.ja-begge"
            med "svar.nei-kun-dagpenger"
            med "svar.nei-kun-forskudd-lonnsgarantimidler" id `arbeidsforhold dagpenger og forskudd lonnsgarantimidler`,
        boolsk faktum "faktum.arbeidsforhold-godta-nav-trekk-direkte-lonnsgaranti" id `arbeidsforhold godta nav trekk direkte lonnsgaranti`,
        envalg faktum "faktum.arbeidsforhold-sok-lonnsgarantimidler"
            med "svar.ja-allerede-sendt"
            med "svar.ja-skal-sende"
            med "svar.nei" id `arbeidsforhold sok lonnsgarantimidler`,
        envalg faktum "faktum.arbeidsforhold-lonnsgaranti-dekker-krav"
            med "svar.nei"
            med "svar.ja"
            med "svar.vet-ikke" id `arbeidsforhold lonnsgaranti dekker krav`,
        boolsk faktum "faktum.arbeidsforhold-godta-trekk-direkte-konkursbo" id `arbeidsforhold godta trekk direkte konkursbo`,
        boolsk faktum "faktum.arbeidsforhold-utbetalt-lonn-etter-konkurs" id `arbeidsforhold utbetalt lonn etter konkurs`,
        dato faktum "faktum-arbeidsforhold-konkurs-siste-dag-lonn" id `faktum arbeidsforhold konkurs siste dag lonn`,
        tekst faktum "faktum.arbeidsforhold-tillegsinformasjon" id `arbeidsforhold tillegsinformasjon`,
        heltall faktum "faktum.arbeidsforhold" id arbeidsforhold
            genererer `navn bedrift`
            og `arbeidsforhold land`
            og `arbeidsforhold aarsak`,
        tekst faktum "faktum.navn-bedrift" id `navn bedrift`,
        envalg faktum "faktum.arbeidsforhold-land"
            med "" id `arbeidsforhold land`,
        envalg faktum "faktum.arbeidsforhold-aarsak"
            med "svar.sagt-opp-av-arbeidsgiver"
            med "svar.permittert"
            med "svar.kontrakt-utgaatt"
            med "svar.sagt-opp-selv"
            med "svar.redusert-arbeidstid"
            med "svar.konkurs-arbeidsgiver"
            med "svar.avskjediget"
            med "svar.ikke-endret" id `arbeidsforhold aarsak`
    )

    private val alleVariabler = listOf(
        `dagpenger soknadsdato`,
        `fast arbeidstid`,
        arbeidsforhold,
        `navn bedrift`,
        `arbeidsforhold land`,
        `arbeidsforhold aarsak`,
        `arbeidsforhold varighet`,
        `arbeidsforhold ekstra opplysninger laerlig`,
        `arbeidsforhold ekstra opplysninger fiskeindustri`,
        `arbeidsforhold ekstra opplysninger flere arbeidsforhold`,
        `arbeidsforhold arbeidstid timer i uken alle forhold`,
        `arbeidsforhold arbeidstid timer i uken`,
        `arbeidsforhold aarsak til oppsigelse fra arbeidsgiver`,
        `arbeidsforhold aarsak til avskjedigelse fra arbeidsgiver`,
        `tilbud annen stilling annet sted samme arbeidsgiver`,
        `tilbud forsette samme arbeidsgiver`,
        `arbeids skift turnus rotasjon`,
        `arbeidsforhold rotasjon antall arbeidsdager`,
        `arbeidsforhold rotasjon antall fridager`,
        `midlertidig arbeidsforhold med sluttdato`,
        `midlertidig arbeidsforhold sluttdato`,
        `arbeidsforhold permitert`,
        `arbeidsforhold permitteringsperiode`,
        `arbeidsforhold permitteringgrad`,
        `arbeidsforhold lonnsplinkt arbeidsgiver`,
        `aarsak til sagt opp selv`,
        `arbeidsforhold arbeidsgiver konkurs navn bostyrer`,
        `arbeidsforhold dagpenger og forskudd lonnsgarantimidler`,
        `arbeidsforhold godta nav trekk direkte lonnsgaranti`,
        `arbeidsforhold sok lonnsgarantimidler`,
        `arbeidsforhold lonnsgaranti dekker krav`,
        `arbeidsforhold godta trekk direkte konkursbo`,
        `arbeidsforhold utbetalt lonn etter konkurs`,
        `faktum arbeidsforhold konkurs siste dag lonn`,
        `arbeidsforhold tillegsinformasjon`,
    )

    fun fakta(): Array<FaktumFactory<*>> = fakta.toTypedArray()
    fun variabler(): IntArray = alleVariabler.toIntArray()
}