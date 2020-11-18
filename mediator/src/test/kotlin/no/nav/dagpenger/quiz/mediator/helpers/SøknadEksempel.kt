package no.nav.dagpenger.quiz.mediator.helpers

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dato
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dokument
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.heltall
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.inntekt
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.ja
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.marshalling.FaktumNavBehov
import no.nav.dagpenger.model.regel.av
import no.nav.dagpenger.model.regel.er
import no.nav.dagpenger.model.regel.minst
import no.nav.dagpenger.model.seksjon.Seksjon
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.model.seksjon.Versjon
import no.nav.dagpenger.model.subsumsjon.alle

internal object SøknadEksempel {

    val prototypeSøknad1 = Søknad(
        ja nei "f1_bool" id 1,
        ja nei "f2_bool" id 2,
        heltall faktum "f3_heltall" id 3,
        dato faktum "f4_dato" id 4,
        inntekt faktum "f5_inntekt" id 5,
        inntekt faktum "f6_inntekt" id 6,
        dokument faktum "f7_dokument" id 7,
        ja nei "f8_bool" id 8,

    )

    private val webPrototypeSøknad = Søknadprosess(
        Seksjon(
            "seksjon1",
            Rolle.nav,
            prototypeSøknad1.ja(1),
            prototypeSøknad1.ja(2)
        ),
        Seksjon(
            "seksjon2",
            Rolle.nav,
            prototypeSøknad1.heltall(3)
        ),
        Seksjon(
            "seksjon3",
            Rolle.søker,
            prototypeSøknad1.dato(4)
        ),
        Seksjon(
            "seksjon4",
            Rolle.søker,
            prototypeSøknad1.inntekt(5),
            prototypeSøknad1.inntekt(6)
        ),
        Seksjon(
            "seksjon5",
            Rolle.søker,
            prototypeSøknad1.dokument(7)
        ),
        Seksjon(
            "seksjon6",
            Rolle.saksbehandler,
            prototypeSøknad1.ja(8)
        )
    )

    private val subsumsjon = "subsumsjon".alle(
        prototypeSøknad1 ja 1 er true,
        prototypeSøknad1 ja 2 er true,
        prototypeSøknad1 heltall 3 er 2,
        prototypeSøknad1 dato 4 er 24.desember,
        prototypeSøknad1 inntekt 5 minst (prototypeSøknad1 inntekt 6),
        prototypeSøknad1 ja 8 av (prototypeSøknad1 dokument 7)
    )

    val v = Versjon(
        prototypeSøknad1,
        subsumsjon,
        mapOf(Versjon.UserInterfaceType.Web to webPrototypeSøknad)
    )

    val faktumNavBehov = FaktumNavBehov(
        Versjon.versjoner.keys.maxOf { it },
        mapOf(
            1 to "f1Behov",
            2 to "f2Behov",
            3 to "f3Behov",
            4 to "f4Behov",
            5 to "f5Behov",
            6 to "f6Behov",
            7 to "f7Behov",
            8 to "f8Behov"
        )
    )
}
