package utils

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dato
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.inntekt
import no.nav.dagpenger.model.fakta.Fakta
import no.nav.dagpenger.model.fakta.Inntekt.Companion.årlig
import no.nav.dagpenger.model.fakta.Rolle
import no.nav.dagpenger.model.regel.er
import no.nav.dagpenger.model.subsumsjon.alle
import no.nav.dagpenger.model.søknad.Faktagrupper
import no.nav.dagpenger.model.søknad.Seksjon
import no.nav.dagpenger.model.søknad.Versjon
import java.time.LocalDate

internal class FødselsdatoTestPrototype {

    companion object {
        const val VERSJON_ID = 1
    }

    private val fakta: Fakta
        get() = Fakta(
            dato faktum "Fødselsdato" id 1,
        )

    val fødselsdato = fakta dato 1

    val inngangsvilkår =
        "Inngangsvilkår".alle(
            "alder".alle(
                fødselsdato er LocalDate.now()
            )
        )

    private val personalia = Seksjon("personalia", Rolle.nav, fødselsdato)

    val faktagrupper: Faktagrupper =
        Faktagrupper(
            personalia,
        )
    private val versjon = Versjon(VERSJON_ID, fakta, inngangsvilkår, mapOf(Versjon.Type.Web to faktagrupper))

    fun søknad(fnr: String) = versjon.søknad(fnr, Versjon.Type.Web)
}

internal class AvhengerAvTestPrototype {
    companion object {
        const val VERSJON_ID = 1
    }

    private val fakta: Fakta
        get() = Fakta(
            dato faktum "Virkningstidspunkt" id 1,
            inntekt faktum "InntektSisteÅr" id 2 avhengerAv 1,
            inntekt faktum "InntektSiste3År" id 3 avhengerAv 1

        )

    val virkningstidpunkt = fakta dato 1
    val inntektSisteÅr = fakta inntekt 2
    val inntektSiste3År = fakta inntekt 3

    val inngangsvilkår =
        "Inngangsvilkår".alle(
            inntektSisteÅr er 100000.årlig
        )

    val faktagrupper: Faktagrupper =
        Faktagrupper(
            Seksjon("Inntekt", Rolle.nav, inntektSiste3År, inntektSisteÅr),
            Seksjon("Virkningstidspunkt", Rolle.søker, virkningstidpunkt)
        )
    private val versjon = Versjon(VERSJON_ID, fakta, inngangsvilkår, mapOf(Versjon.Type.Web to faktagrupper))

    fun søknad(fnr: String) = versjon.søknad(fnr, Versjon.Type.Web)

    fun delvisBesvartSøknad(fnr: String) = søknad(fnr).also { (it dato 1).besvar(LocalDate.now()) }
}
