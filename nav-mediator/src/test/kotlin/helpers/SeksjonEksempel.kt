package helpers

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.ja
import no.nav.dagpenger.model.faktagrupper.Seksjon
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Søknad

internal object SeksjonEksempel {

    val prototypeSøknad1 = Søknad(
        ja nei "Boolean" id 1,
        ja nei "Verneplikt" id 12,
        ja nei "EgenNæring" id 6
    ).also {
        it.ja(1).besvar(true)
    }

    val seksjon1 = Seksjon(
        "seksjon1",
        Rolle.nav,
        prototypeSøknad1.ja(12),
        prototypeSøknad1.ja(1),
    )

    val seksjon2 = Seksjon(
        "seksjon1",
        Rolle.nav,
        prototypeSøknad1.ja(12),
        prototypeSøknad1.ja(1),
        prototypeSøknad1.ja(6),
    )
}
