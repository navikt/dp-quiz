package helpers

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dato
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dokument
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.inntekt
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.ja
import no.nav.dagpenger.model.factory.UtledetFaktumFactory.Companion.maks
import no.nav.dagpenger.model.faktagrupper.Seksjon
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Søknad

internal object SeksjonEksempel {

    val prototypeSøknad1 = Søknad(
        dato faktum "Ønsker dagpenger fra dato" id 1,
        dato faktum "Siste dag med arbeidsplikt" id 2,
        dato faktum "Registreringsdato" id 3,
        dato faktum "Siste dag med lønn" id 4,
        maks dato "Virkningstidspunkt" av 1 og 2 og 3 og 4 og 11 id 5,
        ja nei "EgenNæring" id 6,
        inntekt faktum "Inntekt siste 3 år" id 7 avhengerAv 5 og 6,
        inntekt faktum "Inntekt siste 12 mnd" id 8 avhengerAv 5 og 6,
        inntekt faktum "3G" id 9,
        inntekt faktum "1,5G" id 10,
        dato faktum "Søknadstidspunkt" id 11,
        ja nei "Verneplikt" id 12,
        dokument faktum "dokumentasjon for fangst og fisk" id 14 avhengerAv 6,
        ja nei "Boolean" id 100

    ).also {
        it.ja(100).besvar(true)
    }

    val seksjon1 = Seksjon(
        "seksjon1",
        Rolle.nav,
        prototypeSøknad1.ja(12),
        prototypeSøknad1.ja(100)
    )

    val seksjon2 = Seksjon(
        "seksjon2",
        Rolle.nav,
        prototypeSøknad1.ja(12),
        prototypeSøknad1.ja(100),
        prototypeSøknad1.ja(6)
    )

    val seksjon3 = Seksjon(
        "seksjon3",
        Rolle.nav,
        prototypeSøknad1.ja(6),
        prototypeSøknad1.ja(7)
    )

    val seksjon4 = Seksjon(
        "seksjon4",
        Rolle.nav,
        prototypeSøknad1.dato(1),
        prototypeSøknad1.dato(2),
        prototypeSøknad1.dato(3),
        prototypeSøknad1.dato(4),
        prototypeSøknad1.dato(5),
        prototypeSøknad1.ja(6),
        prototypeSøknad1.inntekt(7),
        prototypeSøknad1.dato(11)
    )

    val seksjonAlle = Seksjon(
        "seksjon5",
        Rolle.nav,
        prototypeSøknad1.dato(1),
        prototypeSøknad1.dato(2),
        prototypeSøknad1.dato(3),
        prototypeSøknad1.dato(4),
        prototypeSøknad1.dato(5),
        prototypeSøknad1.ja(6),
        prototypeSøknad1.inntekt(7),
        prototypeSøknad1.inntekt(8),
        prototypeSøknad1.inntekt(9),
        prototypeSøknad1.inntekt(10),
        prototypeSøknad1.dato(11),
        prototypeSøknad1.ja(12),
        prototypeSøknad1.dokument(14)
    )

    val seksjonFaktum14 = Seksjon(
        "seksjonFaktum14",
        Rolle.nav,
        prototypeSøknad1.dokument(14)
    )
}
