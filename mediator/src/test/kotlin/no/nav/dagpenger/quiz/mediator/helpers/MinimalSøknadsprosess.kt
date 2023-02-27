package no.nav.dagpenger.quiz.mediator.helpers

import mu.KotlinLogging
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.boolsk
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.heltall
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.tekst
import no.nav.dagpenger.model.faktum.Fakta
import no.nav.dagpenger.model.faktum.Faktaversjon
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.marshalling.FaktumNavBehov
import no.nav.dagpenger.model.regel.er
import no.nav.dagpenger.model.regel.minst
import no.nav.dagpenger.model.regel.utfylt
import no.nav.dagpenger.model.seksjon.FaktaVersjonDingseboms
import no.nav.dagpenger.model.seksjon.Prosess
import no.nav.dagpenger.model.seksjon.Prosessversjon
import no.nav.dagpenger.model.seksjon.Seksjon
import no.nav.dagpenger.model.subsumsjon.Subsumsjon
import no.nav.dagpenger.model.subsumsjon.alle

internal class MinimalSøknadsprosess(private val faktaversjon: Faktaversjon, private val rolle: Rolle) {
    private val logger = KotlinLogging.logger { }
    internal val fakta = Fakta(
        faktaversjon,
        boolsk faktum "boolean" id faktumBoolsk,
        heltall faktum "heltall" id faktumHeltall,
        tekst faktum "tekst" id faktumTekst,
    )
    val regeltre: Subsumsjon =
        with(fakta) {
            "alle".alle(
                boolsk(faktumBoolsk) er true,
                heltall(faktumHeltall) minst (0),
                heltall(faktumTekst).utfylt(),
            )
        }
    val seksjoner = Seksjon(
        "test",
        rolle,
        fakta.boolsk(faktumBoolsk),
        fakta.heltall(faktumHeltall),
        fakta.tekst(faktumTekst),
    )

    companion object {
        const val faktumBoolsk = 1
        const val faktumHeltall = 2
        const val faktumTekst = 3
    }

    fun registrer(registrer: (fakta: Fakta) -> Unit) {
        registrer(fakta)
    }

    private val søknadsprosess: Prosess = Prosess(
        Testprosess.Test,
        seksjoner,
    )
    private val faktumNavBehov = FaktumNavBehov(
        mapOf(
            faktumBoolsk to "faktumBoolsk",
            faktumHeltall to "faktumHeltall",
            faktumTekst to "faktumTekst",
        ),
    )

    init {
        FaktaVersjonDingseboms.Bygger(
            fakta,
        ).registrer()

        Prosessversjon.Bygger(
            faktatype = faktaversjon.faktatype,
            prototypeSubsumsjon = regeltre,
            prosess = søknadsprosess,
            faktumNavBehov = faktumNavBehov,
        ).registrer().also {
            logger.info { "\n\n\nREGISTRERT versjon id $faktaversjon \n\n\n\n" }
        }
    }
}
