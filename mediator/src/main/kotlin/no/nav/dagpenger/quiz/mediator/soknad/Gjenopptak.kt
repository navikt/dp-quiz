package no.nav.dagpenger.quiz.mediator.soknad

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.boolsk
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.regel.er
import no.nav.dagpenger.model.seksjon.Seksjon
import no.nav.dagpenger.model.seksjon.Søknadprosess

internal object Gjenopptak {

    const val VERSJON_ID = 100

    const val gjenopptak = 1

    fun registrer(registrer: (søknad: Søknad, versjonId: Int) -> Unit) {
        registrer(Gjenopptak.søknad, Gjenopptak.VERSJON_ID)
    }

    internal val søknad: Søknad
        get() = Søknad(
            VERSJON_ID,
            boolsk faktum "Har du hatt dagpenger siste 52 uker" id gjenopptak,
        )
}

internal object GjenopptakSeksjoner {
    internal val gjenopptakSøknadsprosess: Søknadprosess =
        Søknadprosess(
            GjenopptakSeksjon.gjenopptak
        )
}

internal object GjenopptakSeksjon {
    val gjenopptak = with(Gjenopptak.søknad) {
        Seksjon("gjenopptak", Rolle.søker, dato(Gjenopptak.gjenopptak))
    }
}

internal object SkalBeslutteGjenopptak {
    val sjekkGjenopptak = with(Gjenopptak.søknad) {
        boolsk(Gjenopptak.gjenopptak) er true
    }
}
