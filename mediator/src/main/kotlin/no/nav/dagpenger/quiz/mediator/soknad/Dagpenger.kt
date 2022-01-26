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

    const val `for dummy-boolean` = 1
    const val `for dummy-envalg` = 2
    const val `for dummy-tekst med avhengighet` = 3
    const val `for dummy-flervalg` = 4
    const val `for dummy-heltall` = 5
    const val `for dummy-desimaltall` = 6
    const val `for dummy-tekst` = 7
    const val `for dummy-dato` = 8
    const val `for dummy-periode` = 9
    const val `for dummy-generator` = 10
    const val `for generator dummy-boolean` = 11
    const val `for generator dummy-envalg` = 12
    const val `for generator dummy-tekst med avhengighet` = 13
    const val `for generator dummy-flervalg` = 14
    const val `for generator dummy-heltall` = 15
    const val `for generator dummy-desimaltall` = 16
    const val `for generator dummy-tekst` = 17
    const val `for generator dummy-dato` = 18
    const val `for generator dummy-periode` = 19

    fun registrer(registrer: (søknad: Søknad) -> Unit) {
        registrer(søknad)
    }

    internal val søknad: Søknad
        get() {
            val søknad1 = Søknad(
                VERSJON_ID,
                boolsk faktum "faktum.dummy-boolean" id `for dummy-boolean`,
                envalg faktum "faktum.dummy-valg"
                    med "faktum.dummy-valg.svar.ja"
                    med "faktum.dummy-valg.svar.nei"
                    med "faktum.dummy-valg.svar.vetikke" id `for dummy-envalg`,
                tekst faktum "faktum.dummy-tekst" id `for dummy-tekst med avhengighet` avhengerAv `for dummy-envalg`,
                flervalg faktum "faktum.dummy-flervalg"
                    med "faktum.dummy-flervalgsvar.1"
                    med "faktum.dummy-flervalgsvar.2"
                    med "faktum.dummy-flervalgsvar.3" id `for dummy-flervalg`,
                heltall faktum "faktum.dummy-int" id `for dummy-heltall`,
                desimaltall faktum "faktum.dummy-desimaltall" id `for dummy-desimaltall`,
                tekst faktum "faktum.dummy-tekst" id `for dummy-tekst`,
                dato faktum "faktum.dummy-localdate" id `for dummy-dato`,
                periode faktum "faktum.dummy-periode" id `for dummy-periode`,
                heltall faktum "faktum.dummy-generator" id `for dummy-generator`
                    genererer `for generator dummy-boolean`
                    og `for generator dummy-envalg`
                    og `for generator dummy-tekst med avhengighet`
                    og `for generator dummy-flervalg`
                    og `for generator dummy-heltall`
                    og `for generator dummy-desimaltall`
                    og `for generator dummy-tekst`
                    og `for generator dummy-dato`
                    og `for generator dummy-periode`,
                boolsk faktum "faktum.generator-dummy-boolean" id `for generator dummy-boolean`,
                envalg faktum "faktum.generator-dummy-valg"
                    med "faktum.generator-dummy-valg.svar.ja"
                    med "faktum.generator-dummy-valg.svar.nei"
                    med "faktum.generator-dummy-valg.svar.vetikke" id `for generator dummy-envalg`,
                tekst faktum "faktum.generator-dummy-tekst" id `for generator dummy-tekst med avhengighet` avhengerAv `for generator dummy-envalg`,
                flervalg faktum "faktum.generator-dummy-flervalg"
                    med "faktum.dummy-flervalgsvar.1"
                    med "faktum.dummy-flervalgsvar.2"
                    med "faktum.dummy-flervalgsvar.3" id `for generator dummy-flervalg`,
                heltall faktum "faktum.generator-dummy-int" id `for generator dummy-heltall`,
                desimaltall faktum "faktum.generator-dummy-desimaltall" id `for generator dummy-desimaltall`,
                tekst faktum "faktum.generator-dummy-tekst" id `for generator dummy-tekst`,
                dato faktum "faktum.generator-dummy-localdate" id `for generator dummy-dato`,
                periode faktum "faktum.generator-dummy-periode" id `for generator dummy-periode`
            )
            return søknad1
        }

    private object Seksjoner {

        val søkerSeksjon = søknad.seksjon(
            "søkerseksjon",
            Rolle.søker,
            `for dummy-boolean`,
            `for dummy-envalg`,
            `for dummy-tekst med avhengighet`,
            `for dummy-flervalg`,
            `for dummy-heltall`,
            `for dummy-desimaltall`,
            `for dummy-tekst`,
            `for dummy-dato`,
            `for dummy-periode`,
            `for dummy-generator`
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
                heltall(`for dummy-heltall`) minst (0)
//                heltall(`personalia alder`) minst (0),
//                tekst(`personalia navn`).utfylt(),
//                periode(`en eller annen period`).utfylt()
            )
        }
    }

    private val faktumNavBehov =
        FaktumNavBehov(
            mapOf(
                `for dummy-heltall` to "trengerEtHeltall",
//                `personalia alder` to "PersonaliaAlder",
//                `personalia navn` to "PersonaliaNavn",
//                `en eller annen period` to "enEllerAnnenPeriod"
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
