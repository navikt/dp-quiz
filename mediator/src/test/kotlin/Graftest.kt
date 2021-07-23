
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.boolsk
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dato
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.heltall
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.inntekt
import no.nav.dagpenger.model.factory.UtledetFaktumFactory.Companion.maks
import no.nav.dagpenger.model.faktum.Identer
import no.nav.dagpenger.model.faktum.Person
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.marshalling.SubsumsjonsGraf
import no.nav.dagpenger.model.regel.er
import no.nav.dagpenger.model.regel.før
import no.nav.dagpenger.model.regel.har
import no.nav.dagpenger.model.regel.mellom
import no.nav.dagpenger.model.regel.minst
import no.nav.dagpenger.model.seksjon.Seksjon
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.model.seksjon.Versjon
import no.nav.dagpenger.model.subsumsjon.alle
import no.nav.dagpenger.model.subsumsjon.deltre
import no.nav.dagpenger.model.subsumsjon.hvisGyldig
import no.nav.dagpenger.model.subsumsjon.hvisUgyldig
import no.nav.dagpenger.model.subsumsjon.hvisUgyldigManuell
import no.nav.dagpenger.model.subsumsjon.minstEnAv
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntekt
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett
import no.nav.dagpenger.quiz.mediator.soknad.Seksjoner
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.util.UUID

class Graftest {

    @Test
    @Disabled
    fun `tegne subsumsjonsgraf`() {
        val bursdag67 = 1
        val søknadsdato = 2
        val ønsketdato = 3
        val sisteDagMedLønn = 4
        val inntektSiste3år = 5
        val inntektSisteÅr = 6
        val dimisjonsdato = 7
        val virkningsdato = 8
        val inntekt3G = 9
        val inntekt15G = 10
        val manuell = 11
        val registrertArbeidssøker = 12
        val registrertArbeidssøkerPerioder = 13
        val registrertArbeidssøkerPeriodeTom = 15

        val prototypeSøknad = Søknad(
            509,
            dato faktum "Datoen du fyller 67" id bursdag67,
            dato faktum "Datoen du søker om dagpenger" id søknadsdato,
            dato faktum "Datoen du ønsker dagpenger fra" id ønsketdato,
            dato faktum "Siste dag du mottar lønn" id sisteDagMedLønn,
            inntekt faktum "Inntekt siste 36 måneder" id inntektSiste3år,
            inntekt faktum "Inntekt siste 12 måneder" id inntektSisteÅr,
            dato faktum "Dimisjonsdato" id dimisjonsdato,
            maks dato "Hvilken dato vedtaket skal gjelde fra" av 2 og 3 og 4 id virkningsdato,
            inntekt faktum "3G" id inntekt3G,
            inntekt faktum "1.5G" id inntekt15G,
            boolsk faktum "Manuell fordi noe" id manuell,
            boolsk faktum "Registrert arbeidssøker" id registrertArbeidssøker,
            heltall faktum "Antall arbeidsøker registeringsperioder" id registrertArbeidssøkerPerioder genererer registrertArbeidssøkerPeriodeTom,
            dato faktum "arbeidssøker til" id registrertArbeidssøkerPeriodeTom
            )

        val prototypeWebSøknad =
            with(prototypeSøknad) {
                Søknadprosess(
                    Seksjon(
                        "seksjon1",
                        Rolle.søker,
                        dato(bursdag67),
                        dato(søknadsdato),
                        dato(ønsketdato),
                        dato(sisteDagMedLønn),
                        dato(dimisjonsdato),
                        dato(virkningsdato),
                        inntekt(inntekt15G),
                        inntekt(inntekt3G),
                        inntekt(inntektSiste3år),
                        inntekt(inntektSisteÅr),
                        boolsk(registrertArbeidssøker)
                    ),
                    Seksjon(
                        "manuell",
                        Rolle.manuell,
                        boolsk(manuell)
                    )
                )
            }

        val prototypeSubsumsjon = with(prototypeSøknad) {
            boolsk(registrertArbeidssøker) er true hvisGyldig {
                generator(registrertArbeidssøkerPerioder) har "arbeidsøkerregistrering".deltre {
                    dato(søknadsdato) før dato(registrertArbeidssøkerPeriodeTom)
                }
            } hvisUgyldig {
                dato(bursdag67) før dato(søknadsdato) hvisGyldig {
                    "bursdagssjekker".alle(
                        dato(bursdag67) før dato(sisteDagMedLønn) hvisGyldig { dato(bursdag67) før dato(bursdag67) },
                        "flere sjekker".minstEnAv(
                            dato(bursdag67) før dato(dimisjonsdato)
                        ) hvisGyldig { dato(ønsketdato) før dato(ønsketdato) },
                        "enda flere sjekker ".minstEnAv(
                            inntekt(inntekt15G) minst inntekt(inntekt3G),
                            inntekt(inntekt15G) minst inntekt(inntekt15G)
                        )
                    ) hvisGyldig {
                        "minst ein".minstEnAv(dato(sisteDagMedLønn) før dato(sisteDagMedLønn))
                    } hvisUgyldig {
                        "deltre".deltre { inntekt(inntektSisteÅr) minst inntekt(inntekt15G) hvisUgyldigManuell (boolsk(manuell)) }
                    }
                }
            }
        }

        val søknadprosess = Versjon.Bygger(
            prototypeSøknad,
            prototypeSubsumsjon,
            mapOf(Versjon.UserInterfaceType.Web to prototypeWebSøknad)
        )
            .søknadprosess(
                Person(UUID.randomUUID(), Identer.Builder().folkeregisterIdent("12345678910").build()),
                Versjon.UserInterfaceType.Web
            )

        SubsumsjonsGraf(søknadprosess).skrivTilFil("example/ex2.png")
        Runtime.getRuntime().exec("open example/ex2.png")
    }

    @Test
    @Disabled
    fun `avslag`() {

        val manglerInntekt = Versjon.Bygger(
            AvslagPåMinsteinntektOppsett.søknad,
            AvslagPåMinsteinntekt.regeltre,
            mapOf(Versjon.UserInterfaceType.Web to Seksjoner.søknadprosess)
        )
            .søknadprosess(
                Person(UUID.randomUUID(), Identer.Builder().folkeregisterIdent("12345678910").build()),
                Versjon.UserInterfaceType.Web
            )

        SubsumsjonsGraf(manglerInntekt).skrivTilFil("example/ex2.png")
        Runtime.getRuntime().exec("open example/ex2.png")
    }
}
