package no.nav.dagpenger.quiz.mediator.helpers

import mu.KotlinLogging
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.boolsk
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.heltall
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.tekst
import no.nav.dagpenger.model.faktum.Prosessversjon
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.marshalling.FaktumNavBehov
import no.nav.dagpenger.model.regel.er
import no.nav.dagpenger.model.regel.minst
import no.nav.dagpenger.model.regel.utfylt
import no.nav.dagpenger.model.seksjon.Faktagrupper
import no.nav.dagpenger.model.seksjon.Seksjon
import no.nav.dagpenger.model.seksjon.Versjon
import no.nav.dagpenger.model.subsumsjon.Subsumsjon
import no.nav.dagpenger.model.subsumsjon.alle

internal class MinimalSøknadsprosess(private val prosessversjon: Prosessversjon, private val rolle: Rolle) {

    private val logger = KotlinLogging.logger { }

    internal val søknad = Søknad(
        prosessversjon,
        boolsk faktum "boolean" id faktumBoolsk,
        heltall faktum "heltall" id faktumHeltall,
        tekst faktum "tekst" id faktumTekst
    )

    val regeltre: Subsumsjon =
        with(søknad) {
            "alle".alle(
                boolsk(faktumBoolsk) er true,
                heltall(faktumHeltall) minst (0),
                heltall(faktumTekst).utfylt()
            )
        }

    val seksjoner = Seksjon(
        "test",
        rolle,
        søknad.boolsk(faktumBoolsk),
        søknad.heltall(faktumHeltall),
        søknad.tekst(faktumTekst)
    )

    companion object {
        const val faktumBoolsk = 1
        const val faktumHeltall = 2
        const val faktumTekst = 3
    }

    fun registrer(registrer: (søknad: Søknad) -> Unit) {
        registrer(søknad)
    }

    private val søknadsprosess: Faktagrupper = Faktagrupper(seksjoner)

    private val faktumNavBehov = FaktumNavBehov(
        mapOf(
            faktumBoolsk to "faktumBoolsk",
            faktumHeltall to "faktumHeltall",
            faktumTekst to "faktumTekst",
        )
    )

    private val versjon = Versjon.Bygger(
        prototypeSøknad = søknad,
        prototypeSubsumsjon = regeltre,
        prototypeUserInterfaces = mapOf(
            Versjon.UserInterfaceType.Web to søknadsprosess
        ),
        faktumNavBehov = faktumNavBehov
    ).registrer().also {
        logger.info { "\n\n\nREGISTRERT versjon id $prosessversjon \n\n\n\n" }
    }
}
