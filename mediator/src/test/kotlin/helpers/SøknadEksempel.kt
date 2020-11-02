package helpers

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.ja
import no.nav.dagpenger.model.faktagrupper.Faktagrupper
import no.nav.dagpenger.model.faktagrupper.Seksjon
import no.nav.dagpenger.model.faktagrupper.Versjon
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.regel.er
import no.nav.dagpenger.model.subsumsjon.alle

internal object SøknadEksempel {

    val prototypeSøknad1 = Søknad(
        ja nei "f1" id 1,
        ja nei "f2" id 2
    )

    private val webPrototypeSøknad = Faktagrupper(
        Seksjon(
            "seksjon1",
            Rolle.søker,
            prototypeSøknad1.ja(1)
        ),
        Seksjon(
            "seksjon2",
            Rolle.søker,
            prototypeSøknad1.ja(2)
        )
    )

    val v = Versjon(
        3,
        prototypeSøknad1,
        "reell arbeidssøker".alle(prototypeSøknad1 ja 1 er true, prototypeSøknad1 ja 2 er true),
        mapOf(Versjon.FaktagrupperType.Web to webPrototypeSøknad)
    )
}
