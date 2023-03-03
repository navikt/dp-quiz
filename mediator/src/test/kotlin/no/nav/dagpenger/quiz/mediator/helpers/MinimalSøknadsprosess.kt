package no.nav.dagpenger.quiz.mediator.helpers

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.boolsk
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.heltall
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.tekst
import no.nav.dagpenger.model.faktum.Fakta
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.marshalling.FaktumNavBehov
import no.nav.dagpenger.model.regel.er
import no.nav.dagpenger.model.regel.minst
import no.nav.dagpenger.model.regel.utfylt
import no.nav.dagpenger.model.seksjon.Henvendelser
import no.nav.dagpenger.model.seksjon.Prosess
import no.nav.dagpenger.model.seksjon.Seksjon
import no.nav.dagpenger.model.subsumsjon.Subsumsjon
import no.nav.dagpenger.model.subsumsjon.alle

internal class MinimalSøknadsprosess(rolle: Rolle) {
    private val faktaversjon = testFaktaversjon()
    private val faktumNavBehov = FaktumNavBehov(
        mapOf(
            faktumBoolsk to "faktumBoolsk",
            faktumHeltall to "faktumHeltall",
            faktumTekst to "faktumTekst",
        ),
    )
    internal val prototypeFakta = Fakta(
        faktaversjon,
        boolsk faktum "boolean" id faktumBoolsk,
        heltall faktum "heltall" id faktumHeltall,
        tekst faktum "tekst" id faktumTekst,
    )
    private val prototypeSubsumsjon: Subsumsjon =
        with(prototypeFakta) {
            "alle".alle(
                boolsk(faktumBoolsk) er true,
                heltall(faktumHeltall) minst (0),
                heltall(faktumTekst).utfylt(),
            )
        }
    private val seksjoner = Seksjon(
        "test",
        rolle,
        prototypeFakta.boolsk(faktumBoolsk),
        prototypeFakta.heltall(faktumHeltall),
        prototypeFakta.tekst(faktumTekst),
    )

    companion object {
        const val faktumBoolsk = 1
        const val faktumHeltall = 2
        const val faktumTekst = 3
    }

    fun registrer(registrer: (fakta: Fakta) -> Unit) {
        registrer(prototypeFakta)
    }

    private val prototypeProsess: Prosess = Prosess(
        Testprosess.Test,
        seksjoner,
    )

    fun bygger() = Henvendelser.testProsess(prototypeFakta, prototypeProsess, prototypeSubsumsjon, faktumNavBehov)
}
