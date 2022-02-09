package no.nav.dagpenger.quiz.mediator.soknad.dagpenger

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.boolsk
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.desimaltall
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.envalg
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.flervalg
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.heltall
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.tekst
import no.nav.dagpenger.quiz.mediator.soknad.DslFaktaseksjon

object EgenNæring : DslFaktaseksjon {
    const val `driver du egen naering` = 3001
    const val `egen naering organisasjonsnummer liste` = 3002
    const val `egen naering organisasjonsnummer` = 3003
    const val `egen naering arbeidstimer` = 3004
    const val `driver du eget gaardsbruk` = 3005
    const val `faktum eget gaardsbruk organisasjonsnummer` = 3006
    const val `faktum eget gaardsbruk type gaardsbruk` = 3007
    const val `faktum eget gaardsbruk hvem eier` = 3008
    const val `faktum eget gaardsbruk arbeidstimer` = 3009
    const val `faktum eget gaardsbruk arbeidsaar` = 3010
    const val `faktum eget gaardsbruk arbeidstimer beregning` = 3011

    override val fakta = listOf(
        boolsk faktum "faktum.driver-du-egen-naering" id `driver du egen naering`,
        heltall faktum "faktum.egen-naering-organisasjonsnummer-liste" id `egen naering organisasjonsnummer liste`
            genererer `egen naering organisasjonsnummer`,
        heltall faktum "faktum.egen-naering-organisasjonsnummer" id `egen naering organisasjonsnummer`,
        desimaltall faktum "faktum.egen-naering-arbeidstimer" id `egen naering arbeidstimer`,
        boolsk faktum "faktum.driver-du-eget-gaardsbruk" id `driver du eget gaardsbruk`,
        heltall faktum "faktum-eget-gaardsbruk-organisasjonsnummer" id `faktum eget gaardsbruk organisasjonsnummer`,
        flervalg faktum "faktum-eget-gaardsbruk-type-gaardsbruk"
            med "faktum.eget-gaardsbruk-type-gaardsbruk.svar.dyr"
            med "faktum.eget-gaardsbruk-type-gaardsbruk.svar.jord"
            med "faktum.eget-gaardsbruk-type-gaardsbruk.svar.skog"
            med "faktum.eget-gaardsbruk-type-gaardsbruk.svar.annet" id `faktum eget gaardsbruk type gaardsbruk`,
        flervalg faktum "faktum-eget-gaardsbruk-hvem-eier"
            med "faktum.eget-gaardsbruk-hvem-eier.svar.selv"
            med "faktum.eget-gaardsbruk-hvem-eier.svar.ektefelle-samboer"
            med "faktum.eget-gaardsbruk-hvem-eier.svar.andre" id `faktum eget gaardsbruk hvem eier`,
        desimaltall faktum "faktum-eget-gaardsbruk-arbeidstimer" id `faktum eget gaardsbruk arbeidstimer`,
        envalg faktum "faktum-eget-gaardsbruk-arbeidsaar"
            med "svar.2022"
            med "svar.2021"
            med "svar.2020"
            med "svar.2019"
            med "svar.2018" id `faktum eget gaardsbruk arbeidsaar`,
        tekst faktum "faktum-eget-gaardsbruk-arbeidstimer-beregning" id `faktum eget gaardsbruk arbeidstimer beregning`
    )
}
