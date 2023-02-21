package no.nav.dagpenger.quiz.mediator.helpers

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.boolsk
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dato
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dokument
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.heltall
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.inntekt
import no.nav.dagpenger.model.faktum.Fakta
import no.nav.dagpenger.model.faktum.Faktaversjon
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.helpers.desember
import no.nav.dagpenger.model.marshalling.FaktumNavBehov
import no.nav.dagpenger.model.regel.dokumenteresAv
import no.nav.dagpenger.model.regel.er
import no.nav.dagpenger.model.regel.minst
import no.nav.dagpenger.model.seksjon.Prosess
import no.nav.dagpenger.model.seksjon.Seksjon
import no.nav.dagpenger.model.seksjon.Versjon
import no.nav.dagpenger.model.subsumsjon.alle

internal object SøknadEksempel {

    val prosessVersjon = Faktaversjon(Testprosess.Test, 666)

    val prototypeFakta1 = Fakta(
        prosessVersjon,
        boolsk faktum "f1_bool" id 1 avhengerAv 17,
        boolsk faktum "f2_bool" id 2,
        heltall faktum "f3_heltall" id 3,
        dato faktum "f4_dato" id 4,
        inntekt faktum "f5_inntekt" id 5,
        inntekt faktum "f6_inntekt" id 6,
        dokument faktum "f7_dokument" id 7,
        boolsk faktum "f8_bool" id 8,
        dokument faktum "innsendt søknadsid" id 17, // MottattSøknadService trenger dette faktumet
        dokument faktum "arena fagsakid" id 52, // MottattSøknadService trenger dette faktumet
    )

    private val webPrototypeSøknad = Prosess(
        Seksjon(
            "seksjon1",
            Rolle.nav,
            prototypeFakta1.boolsk(1),
            prototypeFakta1.boolsk(2),
        ),
        Seksjon(
            "seksjon2",
            Rolle.nav,
            prototypeFakta1.heltall(3),
        ),
        Seksjon(
            "seksjon3",
            Rolle.søker,
            prototypeFakta1.dato(4),
        ),
        Seksjon(
            "seksjon4",
            Rolle.søker,
            prototypeFakta1.inntekt(5),
            prototypeFakta1.inntekt(6),
        ),
        Seksjon(
            "seksjon5",
            Rolle.søker,
            prototypeFakta1.dokument(7),
        ),
        Seksjon(
            "seksjon6",
            Rolle.saksbehandler,
            prototypeFakta1.boolsk(8),
        ),
    )

    private val subsumsjon = "subsumsjon".alle(
        prototypeFakta1 boolsk 1 er true,
        prototypeFakta1 boolsk 2 er true,
        prototypeFakta1 heltall 3 er 2,
        prototypeFakta1 dato 4 er 24.desember,
        prototypeFakta1 inntekt 5 minst (prototypeFakta1 inntekt 6),
        prototypeFakta1 boolsk 8 dokumenteresAv (prototypeFakta1 dokument 7),
    )

    val faktumNavBehov = FaktumNavBehov(
        mapOf(
            1 to "f1Behov",
            2 to "f2Behov",
            3 to "f3Behov",
            4 to "f4Behov",
            5 to "f5Behov",
            6 to "f6Behov",
            7 to "f7Behov",
            8 to "f8Behov",
            17 to "InnsendtSøknadsId",
        ),
    )
    val versjon = Versjon.Bygger(
        prototypeFakta1,
        subsumsjon,
        webPrototypeSøknad,
        faktumNavBehov,
    ).registrer()
}
