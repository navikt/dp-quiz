package no.nav.dagpenger.quiz.mediator.helpers

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dato
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dokument
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.heltall
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.inntekt
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.ja
import no.nav.dagpenger.model.factory.UtledetFaktumFactory.Companion.maks
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.regel.er
import no.nav.dagpenger.model.seksjon.Seksjon
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.model.seksjon.Versjon

internal class SøknadEksempel1 private constructor() {
    companion object {
        val søknadEksempel1 = SøknadEksempel1()
    }
    internal val prototypeFakta1 = Søknad(
        15,
        ja nei "f1" id 1 avhengerAv 11,
        dato faktum "f2" id 2,
        dato faktum "f3" id 3,
        dato faktum "f4" id 4,
        dato faktum "f5" id 5,
        inntekt faktum "f6" id 6,
        inntekt faktum "f7" id 7,
        inntekt faktum "f8" id 8,
        inntekt faktum "f9" id 9,
        ja nei "f10" id 10,
        dokument faktum "f11" id 11,
        ja nei "f12" id 12 avhengerAv 11,
        dato faktum "f13" id 13,
        ja nei "f14" id 14,
        heltall faktum "f15" id 15 genererer 16 og 17 og 18,
        heltall faktum "f16" id 16,
        ja nei "f17" id 17,
        ja nei "f18" id 18,
        ja nei "f19" id 19 avhengerAv 2 og 13,
        maks dato "345" av 3 og 4 og 5 id 345,
        maks dato "345213" av 345 og 2 og 13 id 345213
    )

    private val webPrototypeSøknad = Søknadprosess(
        Seksjon(
            "seksjon",
            Rolle.søker,
            *(prototypeFakta1.map { it }.toTypedArray())
        )
    )

    private val mobilePrototypeSøknad = Søknadprosess(
        Seksjon(
            "seksjon",
            Rolle.søker,
            *(prototypeFakta1.map { it }.toTypedArray())
        ),
        Seksjon(
            "template seksjon",
            Rolle.søker,
            prototypeFakta1.heltall(16),
            prototypeFakta1.ja(17)
        )
    )

    val v = Versjon.Bygger(
        prototypeFakta1,
        prototypeFakta1 ja 1 er true,
        mapOf(
            Versjon.UserInterfaceType.Web to webPrototypeSøknad,
            Versjon.UserInterfaceType.Mobile to mobilePrototypeSøknad
        )
    ).registrer()
}
