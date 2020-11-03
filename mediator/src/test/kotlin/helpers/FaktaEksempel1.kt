package helpers

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dato
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dokument
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.heltall
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.inntekt
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.ja
import no.nav.dagpenger.model.factory.UtledetFaktumFactory.Companion.maks
import no.nav.dagpenger.model.factory.ValgFaktumFactory.Companion.valg
import no.nav.dagpenger.model.faktagrupper.Faktagrupper
import no.nav.dagpenger.model.faktagrupper.Seksjon
import no.nav.dagpenger.model.faktagrupper.Versjon
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.regel.er

internal object FaktaEksempel1 {

    val prototypeFakta1 = Søknad(
        ja nei "f1" id 1,
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
        maks dato "345213" av 345 og 2 og 13 id 345213,
        valg faktum "f20" ja "ja1" ja "ja2" nei "nei1" nei "nei2" id 20
    )

    private val webPrototypeSøknad = Faktagrupper(
        Seksjon(
            "seksjon",
            Rolle.søker,
            *(prototypeFakta1.map { it }.toTypedArray())
        )
    )

    val v = Versjon(
        1,
        prototypeFakta1,
        prototypeFakta1 ja 1 er true,
        mapOf(Versjon.FaktagrupperType.Web to webPrototypeSøknad)
    )
}
