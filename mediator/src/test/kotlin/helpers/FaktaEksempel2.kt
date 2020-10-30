package helpers

import no.nav.dagpenger.model.factory.BaseFaktumFactory
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dato
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dokument
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.heltall
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.inntekt
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.ja
import no.nav.dagpenger.model.factory.UtledetFaktumFactory.Companion.maks
import no.nav.dagpenger.model.fakta.Fakta
import no.nav.dagpenger.model.fakta.Rolle
import no.nav.dagpenger.model.regel.er
import no.nav.dagpenger.model.søknad.Seksjon
import no.nav.dagpenger.model.søknad.Søknad
import no.nav.dagpenger.model.søknad.Versjon

internal object FaktaEksempel2 {

    internal enum class Valg { A, B, C }

    private object valg { infix fun faktum(navn: String) = BaseFaktumFactory(Valg::class.java, navn) }

    val prototypeFakta2 = Fakta(
        ja nei "f1" id 1,
        dato faktum "f2" id 2,
        dato faktum "f3" id 3,
        dato faktum "f4" id 4,
        dato faktum "f5" id 5,
        inntekt faktum "f6" id 6,
        inntekt faktum "f7" id 7,
        inntekt faktum "f8" id 8,
        valg faktum "f9" id 9,
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

    private val webPrototypeSøknad = Søknad(
        Seksjon(
            "seksjon",
            Rolle.søker,
            *(prototypeFakta2.map { it }.toTypedArray())
        )
    )

    val v = Versjon(
        2,
        prototypeFakta2,
        prototypeFakta2 ja 1 er true,
        mapOf(Versjon.Type.Web to webPrototypeSøknad)
    )
}
