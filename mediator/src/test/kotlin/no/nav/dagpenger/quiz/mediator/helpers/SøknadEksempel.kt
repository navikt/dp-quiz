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
import no.nav.dagpenger.model.seksjon.Henvendelser
import no.nav.dagpenger.model.seksjon.Prosess
import no.nav.dagpenger.model.seksjon.Seksjon
import no.nav.dagpenger.model.subsumsjon.alle

internal object SøknadEksempel {
    val prosesstype = Testprosess.Test
    val faktaversjon = Faktaversjon(Testfakta.Test, 666)
    val faktumNavBehov =
        FaktumNavBehov(
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
    val prototypeFakta =
        Fakta(
            faktaversjon,
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
    private val prosess =
        Prosess(
            prosesstype,
            Seksjon(
                "seksjon1",
                Rolle.nav,
                prototypeFakta.boolsk(1),
                prototypeFakta.boolsk(2),
            ),
            Seksjon(
                "seksjon2",
                Rolle.nav,
                prototypeFakta.heltall(3),
            ),
            Seksjon(
                "seksjon3",
                Rolle.søker,
                prototypeFakta.dato(4),
            ),
            Seksjon(
                "seksjon4",
                Rolle.søker,
                prototypeFakta.inntekt(5),
                prototypeFakta.inntekt(6),
            ),
            Seksjon(
                "seksjon5",
                Rolle.søker,
                prototypeFakta.dokument(7),
            ),
            Seksjon(
                "seksjon6",
                Rolle.saksbehandler,
                prototypeFakta.boolsk(8),
            ),
        )
    private val prototypeSubsumsjon =
        "subsumsjon".alle(
            prototypeFakta boolsk 1 er true,
            prototypeFakta boolsk 2 er true,
            prototypeFakta heltall 3 er 2,
            prototypeFakta dato 4 er 24.desember,
            prototypeFakta inntekt 5 minst (prototypeFakta inntekt 6),
            prototypeFakta boolsk 8 dokumenteresAv (prototypeFakta dokument 7),
        )

    init {
        Henvendelser
            .FaktaBygger(
                prototypeFakta,
                faktumNavBehov,
            ).also {
                it.leggTilProsess(prosess, prototypeSubsumsjon)
            }.registrer()
            .also {
                println("##### Versjon registrert med prosesstype $prosesstype #####")
            }
    }
}
