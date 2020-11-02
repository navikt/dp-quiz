package helpers

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dato
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.heltall
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
        ja nei "f1_bool" id 1,
        ja nei "f2_bool" id 2,
        heltall faktum "f3_heltall" id 3,
        dato faktum "f4_dato" id 4,
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
        ),
        Seksjon(
            "seksjon3",
            Rolle.søker,
            prototypeSøknad1.heltall(3)
        ),
        Seksjon(
            "seksjon4",
            Rolle.søker,
            prototypeSøknad1.dato(4)
        )
    )

    private val subsumsjon = "reell arbeidssøker".alle(
        prototypeSøknad1 ja 1 er true,
        prototypeSøknad1 ja 2 er true,
        prototypeSøknad1 heltall 3 er 2,
        prototypeSøknad1 dato 4 er 24.desember
    )

    val v = Versjon(
        3,
        prototypeSøknad1,
        subsumsjon,
        mapOf(Versjon.FaktagrupperType.Web to webPrototypeSøknad)
    )
}
