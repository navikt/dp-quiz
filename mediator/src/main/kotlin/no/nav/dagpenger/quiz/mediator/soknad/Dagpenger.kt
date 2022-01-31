package no.nav.dagpenger.quiz.mediator.soknad

import mu.KotlinLogging
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.boolsk
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dato
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.desimaltall
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.envalg
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.flervalg
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.heltall
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.periode
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.tekst
import no.nav.dagpenger.model.faktum.Prosessversjon
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.faktum.Søknad.Companion.seksjon
import no.nav.dagpenger.model.marshalling.FaktumNavBehov
import no.nav.dagpenger.model.regel.minst
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.model.seksjon.Versjon
import no.nav.dagpenger.model.subsumsjon.Subsumsjon
import no.nav.dagpenger.model.subsumsjon.minstEnAv
import no.nav.dagpenger.quiz.mediator.soknad.Dagpenger.Subsumsjoner.regeltre

internal object Dagpenger {

    private val logger = KotlinLogging.logger { }

    val VERSJON_ID = Prosessversjon(Prosess.Dagpenger, 202)

    const val `dummy-boolean` = 1
    const val `dummy-envalg` = 2
    const val `dummy-tekst med avhengighet` = 3
    const val `dummy-flervalg` = 4
    const val `dummy-heltall` = 5
    const val `dummy-desimaltall` = 6
    const val `dummy-tekst` = 7
    const val `dummy-dato` = 8
    const val `dummy-periode` = 9
    const val `dummy-generator` = 10
    const val `generator dummy-boolean` = 11
    const val `generator dummy-envalg` = 12
    const val `generator dummy-tekst med avhengighet` = 13
    const val `generator dummy-flervalg` = 14
    const val `generator dummy-heltall` = 15
    const val `generator dummy-desimaltall` = 16
    const val `generator dummy-tekst` = 17
    const val `generator dummy-dato` = 18
    const val `generator dummy-periode` = 19

    fun registrer(registrer: (søknad: Søknad) -> Unit) {
        registrer(søknad)
    }

    internal val søknad: Søknad
        get() {
            val søknad1 = Søknad(
                VERSJON_ID,
                boolsk faktum "faktum.dummy-boolean" id `dummy-boolean`,
                envalg faktum "faktum.dummy-valg"
                    med "faktum.dummy-valg.svar.ja"
                    med "faktum.dummy-valg.svar.nei"
                    med "faktum.dummy-valg.svar.vetikke" id `dummy-envalg`,
                tekst faktum "faktum.dummy-tekst" id `dummy-tekst med avhengighet` avhengerAv `dummy-envalg`,
                flervalg faktum "faktum.dummy-flervalg"
                    med "faktum.dummy-flervalg.svar.1"
                    med "faktum.dummy-flervalg.svar.2"
                    med "faktum.dummy-flervalg.svar.3" id `dummy-flervalg`,
                heltall faktum "faktum.dummy-int" id `dummy-heltall`,
                desimaltall faktum "faktum.dummy-desimaltall" id `dummy-desimaltall`,
                tekst faktum "faktum.dummy-tekst" id `dummy-tekst`,
                dato faktum "faktum.dummy-localdate" id `dummy-dato`,
                periode faktum "faktum.dummy-periode" id `dummy-periode`,
                heltall faktum "faktum.dummy-generator" id `dummy-generator`
                    genererer `generator dummy-boolean`
                    og `generator dummy-envalg`
                    // og `generator dummy-tekst med avhengighet`
                    og `generator dummy-flervalg`
                    og `generator dummy-heltall`
                    og `generator dummy-desimaltall`
                    og `generator dummy-tekst`
                    og `generator dummy-dato`
                    og `generator dummy-periode`,
                boolsk faktum "faktum.generator-dummy-boolean" id `generator dummy-boolean`,
                envalg faktum "faktum.generator-dummy-valg"
                    med "faktum.generator-dummy-valg.svar.ja"
                    med "faktum.generator-dummy-valg.svar.nei"
                    med "faktum.generator-dummy-valg.svar.vetikke" id `generator dummy-envalg`,
                tekst faktum "faktum.generator-dummy-tekst" id `generator dummy-tekst med avhengighet`, // avhengerAv `generator dummy-envalg`,
                flervalg faktum "faktum.generator-dummy-flervalg"
                    med "faktum.dummy-flervalg.svar.1"
                    med "faktum.dummy-flervalg.svar.2"
                    med "faktum.dummy-flervalg.svar.3" id `generator dummy-flervalg`,
                heltall faktum "faktum.generator-dummy-int" id `generator dummy-heltall`,
                desimaltall faktum "faktum.generator-dummy-desimaltall" id `generator dummy-desimaltall`,
                tekst faktum "faktum.generator-dummy-tekst" id `generator dummy-tekst`,
                dato faktum "faktum.generator-dummy-localdate" id `generator dummy-dato`,
                periode faktum "faktum.generator-dummy-periode" id `generator dummy-periode`
            )
            return søknad1
        }

    private object Seksjoner {

        val søkerSeksjon = søknad.seksjon(
            "søkerseksjon",
            Rolle.søker,
            `dummy-boolean`,
            `dummy-envalg`,
            `dummy-tekst med avhengighet`,
            `dummy-flervalg`,
            `dummy-heltall`,
            `dummy-desimaltall`,
            `dummy-tekst`,
            `dummy-dato`,
            `dummy-periode`,
            `dummy-generator`,
            `generator dummy-boolean`,
            `generator dummy-envalg`,
            `generator dummy-tekst med avhengighet`,
            `generator dummy-flervalg`,
            `generator dummy-heltall`,
            `generator dummy-desimaltall`,
            `generator dummy-tekst`,
            `generator dummy-dato`,
            `generator dummy-periode`
        )

        val navSeksjon = søknad.seksjon(
            "navseksjon",
            Rolle.nav,
        )
    }

    internal val søknadsprosess: Søknadprosess =
        Søknadprosess(
            Seksjoner.søkerSeksjon,
            Seksjoner.navSeksjon
        )

    object Subsumsjoner {

        val regeltre: Subsumsjon = with(søknad) {
            "alle".minstEnAv(
                heltall(`dummy-heltall`) minst (0)
            )
        }
    }

    private val faktumNavBehov =
        FaktumNavBehov(
            mapOf(
                `dummy-heltall` to "trengerEtHeltall",
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
        logger.info { "\n\n\nREGISTRERT versjon id $VERSJON_ID \n\n\n\n" }
    }
}
