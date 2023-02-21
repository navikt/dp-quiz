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
import no.nav.dagpenger.model.faktum.Faktaversjon
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.regel.er
import no.nav.dagpenger.model.seksjon.Prosess
import no.nav.dagpenger.model.seksjon.Seksjon
import no.nav.dagpenger.model.seksjon.Versjon

internal object SøknadEksempel2 {
    val prosessVersjon = Faktaversjon(Testprosess.Test, 889)
    internal val prototypeFakta by lazy {
        Fakta(
            prosessVersjon,
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
            tekst faktum "f20" med "envalg1" med "envalg2" id 20,
            flervalg faktum "f21" med "flervalg1" med "flervalg2" med "flervalg3" id 21,
            heltall faktum "f22" id 22,
            periode faktum "f24" id 24,
            land faktum "f25" id 25,
            heltall faktum "f26" id 26,
            desimaltall faktum "f27" id 27,
            envalg faktum "f28" med "valg1" med "valg2" id 28,
        )
    }
    private val webPrototypeSøknad = Prosess(
        Seksjon(
            "seksjon",
            Rolle.søker,
            *(prototypeFakta.map { it }.toTypedArray()),
        ),
    )
    private val mobilePrototypeSøknad = Prosess(
        Seksjon(
            "seksjon",
            Rolle.søker,
            *(prototypeFakta.map { it }.toTypedArray()),
        ),
        Seksjon(
            "template seksjon",
            Rolle.søker,
            prototypeFakta.heltall(16),
            prototypeFakta.boolsk(17),
        ),
    )
    val v2 by lazy {
        Versjon.Bygger(
            prototypeFakta,
            prototypeFakta boolsk 1 er true,
            webPrototypeSøknad,
        ).registrer()
    }
}
