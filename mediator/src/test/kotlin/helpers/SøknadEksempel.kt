package helpers

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.ja
import no.nav.dagpenger.model.faktagrupper.Faktagrupper
import no.nav.dagpenger.model.faktagrupper.Seksjon
import no.nav.dagpenger.model.faktagrupper.Versjon
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.regel.er

internal object SøknadEksempel {

    val prototypeSøknad1 = Søknad(
        ja nei "f1" id 1,
    )

    private val webPrototypeSøknad = Faktagrupper(
        Seksjon(
            "seksjon",
            Rolle.søker,
            prototypeSøknad1.ja(1)
        )
    )

    val v = Versjon(
        3,
        prototypeSøknad1,
        prototypeSøknad1 ja 1 er true,
        mapOf(Versjon.FaktagrupperType.Web to webPrototypeSøknad)
    )
}
