package no.nav.dagpenger.quiz.mediator.helpers

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.boolsk
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dato
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.desimaltall
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dokument
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.envalg
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.flervalg
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.heltall
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.inntekt
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.land
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.periode
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.tekst
import no.nav.dagpenger.model.factory.UtledetFaktumFactory.Companion.maks
import no.nav.dagpenger.model.faktum.Fakta
import no.nav.dagpenger.model.faktum.Faktatype
import no.nav.dagpenger.model.faktum.Faktaversjon
import no.nav.dagpenger.model.seksjon.Prosesstype

enum class Testfakta(override val id: String) : Faktatype {
    Test("test-r"),
    SøknadEksempel1("SøknadEksempel1"),
}

enum class Testprosess(override val navn: String, override val faktatype: Faktatype) : Prosesstype {
    Test("Test", Testfakta.Test),
    SøknadEksempel1("SøknadEksempel1", Testfakta.SøknadEksempel1),
}

internal object SøknadEksempel1 {
    val faktatype = Testfakta.SøknadEksempel1
    val faktaversjon = Faktaversjon(faktatype, 888)

    internal val prototypeFakta = Fakta(
        faktaversjon,
        boolsk faktum "f1" id 1 avhengerAv 11,
        dato faktum "f2" id 2,
        dato faktum "f3" id 3,
        dato faktum "f4" id 4,
        dato faktum "f5" id 5,
        inntekt faktum "f6" id 6,
        inntekt faktum "f7" id 7,
        inntekt faktum "f8" id 8,
        inntekt faktum "f9" id 9,
        boolsk faktum "f10" id 10,
        dokument faktum "f11" id 11,
        boolsk faktum "f12" id 12 avhengerAv 11,
        dato faktum "f13" id 13,
        boolsk faktum "f14" id 14,
        heltall faktum "f15" id 15 genererer 16 og 17 og 18 avhengerAv 14,
        heltall faktum "f16" id 16,
        boolsk faktum "f17" id 17,
        boolsk faktum "f18" id 18,
        boolsk faktum "f19" id 19 avhengerAv 2 og 13,
        maks dato "345" av 3 og 4 og 5 id 345,
        maks dato "345213" av 345 og 2 og 13 id 345213,
        envalg faktum "f20" med "envalg1" med "envalg2" id 20,
        flervalg faktum "f21" med "flervalg1" med "flervalg2" med "flervalg3" id 21,
        heltall faktum "f22" id 22,
        tekst faktum "f23" id 23,
        periode faktum "f24" id 24,
        land faktum "f25" id 25,
        desimaltall faktum "f26" id 26,
    ).registrer()
}
