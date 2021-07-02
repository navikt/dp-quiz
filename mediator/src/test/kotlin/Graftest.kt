
import guru.nidi.graphviz.attribute.Arrow
import guru.nidi.graphviz.attribute.Color
import guru.nidi.graphviz.attribute.GraphAttr
import guru.nidi.graphviz.attribute.Rank
import guru.nidi.graphviz.engine.Format
import guru.nidi.graphviz.graph
import guru.nidi.graphviz.toGraphviz
import no.nav.dagpenger.model.factory.BaseFaktumFactory
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.boolsk
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dato
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.inntekt
import no.nav.dagpenger.model.factory.UtledetFaktumFactory
import no.nav.dagpenger.model.factory.UtledetFaktumFactory.Companion.maks
import no.nav.dagpenger.model.faktum.Identer
import no.nav.dagpenger.model.faktum.Person
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.marshalling.SubsumsjonsGraf
import no.nav.dagpenger.model.regel.er
import no.nav.dagpenger.model.regel.før
import no.nav.dagpenger.model.regel.minst
import no.nav.dagpenger.model.seksjon.Seksjon
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.model.seksjon.Versjon
import no.nav.dagpenger.model.subsumsjon.alle
import no.nav.dagpenger.model.subsumsjon.deltre
import no.nav.dagpenger.model.subsumsjon.hvisGyldig
import no.nav.dagpenger.model.subsumsjon.hvisGyldigManuell
import no.nav.dagpenger.model.subsumsjon.hvisUgyldig
import no.nav.dagpenger.model.subsumsjon.hvisUgyldigManuell
import no.nav.dagpenger.model.subsumsjon.minstEnAv
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntekt
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett
import no.nav.dagpenger.quiz.mediator.soknad.Seksjoner
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.io.File
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
            boolsk faktum "Registrert arbeidssøker" id registrertArbeidssøker
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
                "deltre".deltre { inntekt(inntektSisteÅr) minst inntekt(inntekt15G) hvisUgyldigManuell (boolsk(manuell)) }
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
                    )
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

        val graf = graph(directed = true) {
            edge["color" eq "black", Arrow.NORMAL]
            node[Color.BLACK]
            graph[Rank.dir(Rank.RankDir.TOP_TO_BOTTOM), GraphAttr.splines(GraphAttr.SplineMode.POLYLINE), GraphAttr.COMPOUND]
        }
        SubsumsjonsGraf(søknadprosess, graf)

        graf.toGraphviz().scale(3.0).render(Format.PNG).toFile(File("example/ex2.png"))
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

        val graf = graph(directed = true) {
            edge["color" eq "black", Arrow.NORMAL]
            node[Color.BLACK]
            graph[Rank.dir(Rank.RankDir.TOP_TO_BOTTOM), GraphAttr.splines(GraphAttr.SplineMode.POLYLINE), GraphAttr.COMPOUND]
        }
        SubsumsjonsGraf(manglerInntekt, graf)
        graf.toGraphviz().scale(2.0).render(Format.PNG).toFile(File("example/ex2.png"))
        Runtime.getRuntime().exec("open example/ex2.png")
    }

    private val ønsketDato = 1
    private val virkningsdato = 4
    private val fangstOgFisk = 5
    private val inntektSiste36mnd = 6
    private val inntektSiste12mnd = 7
    private val minsteinntektfaktor36mnd = 8
    private val minsteinntektfaktor12mnd = 9
    private val minsteinntektsterskel36mnd = 10
    private val minsteinntektsterskel12mnd = 11
    private val grunnbeløp = 12
    private val søknadstidspunkt = 13
    private val verneplikt = 14
    private val innsendtSøknadsId = 17
    private val registrertArbeidssøkerPerioder = 19
    private val lærling = 20
    private val registrertArbeidssøkerPeriodeFom = 21
    private val registrertArbeidssøkerPeriodeTom = 22
    private val behandlingsdato = 23
    private val antallEndredeArbeidsforhold = 26
    private val ordinær = 27
    private val permittert = 28
    private val lønnsgaranti = 29
    private val permittertFiskeforedling = 30
    private val harHattDagpengerSiste36mnd = 32
    private val periodeOppbruktManuell = 33
    private val sykepengerSiste36mnd = 34
    private val svangerskapsrelaterteSykepengerManuell = 35
    private val fangstOgFiskManuell = 36
    private val eøsArbeid = 37
    private val eøsArbeidManuell = 38
    private val uhåndterbartVirkningsdatoManuell = 39
    private val senesteMuligeVirkningsdato = 40
    private val flereArbeidsforholdManuell = 41
    private val oppfyllerMinsteinntektManuell = 42
    private val harInntektNesteKalendermåned = 43
    private val inntektNesteKalendermånedManuell = 44
    private val førsteAvVirkningsdatoOgBehandlingsdato = 45
    private val kanJobbeDeltid = 46
    private val helseTilAlleTyperJobb = 47
    private val kanJobbeHvorSomHelst = 48
    private val villigTilÅBytteYrke = 49
    private val reellArbeidssøkerManuell = 50
    private val registrertArbeidssøkerManuell = 51
    private val G3 = 52
    private val G15 = 53
    private val dato67år = 54
    private val oppfyllerInntekt36MånederManuell = 55
    private val oppfyllerInntekt12MånederManuell = 56
    private val oppfyllerVernepliktManuell = 57
    private val oppfyllerLærlingManuell = 58

    val testSøknad = Søknad(
        348475,
        dato faktum "Ønsker dagpenger fra dato" id ønsketDato avhengerAv innsendtSøknadsId,
        maks dato "Virkningsdato" av ønsketDato og søknadstidspunkt id virkningsdato,
        boolsk faktum "Driver med fangst og fisk" id fangstOgFisk avhengerAv innsendtSøknadsId,
        inntekt faktum "Inntekt siste 36 mnd" id inntektSiste36mnd avhengerAv virkningsdato og fangstOgFisk,
        inntekt faktum "Inntekt siste 12 mnd" id inntektSiste12mnd avhengerAv virkningsdato og fangstOgFisk,
        inntekt faktum "Grunnbeløp" id grunnbeløp avhengerAv virkningsdato,
        BaseFaktumFactory.Companion.desimaltall faktum "Øvre faktor" id minsteinntektfaktor36mnd avhengerAv virkningsdato,
        BaseFaktumFactory.Companion.desimaltall faktum "Nedre faktor" id minsteinntektfaktor12mnd avhengerAv virkningsdato,
        UtledetFaktumFactory.Companion.multiplikasjon inntekt "Minsteinntektsterskel siste 36 mnd" av minsteinntektfaktor36mnd ganger grunnbeløp id minsteinntektsterskel36mnd,
        UtledetFaktumFactory.Companion.multiplikasjon inntekt "Minsteinntektsterskel siste 12 mnd" av minsteinntektfaktor12mnd ganger grunnbeløp id minsteinntektsterskel12mnd,
        dato faktum "Søknadstidspunkt" id søknadstidspunkt avhengerAv innsendtSøknadsId,
        boolsk faktum "Verneplikt" id verneplikt avhengerAv innsendtSøknadsId,
        BaseFaktumFactory.Companion.dokument faktum "Innsendt søknadsId" id innsendtSøknadsId,
        BaseFaktumFactory.Companion.heltall faktum "Antall arbeidsøker registeringsperioder" id registrertArbeidssøkerPerioder genererer registrertArbeidssøkerPeriodeFom og registrertArbeidssøkerPeriodeTom,
        boolsk faktum "Lærling" id lærling avhengerAv innsendtSøknadsId,
        dato faktum "fom" id registrertArbeidssøkerPeriodeFom,
        dato faktum "tom" id registrertArbeidssøkerPeriodeTom,
        dato faktum "Behandlingsdato" id behandlingsdato,
        BaseFaktumFactory.Companion.heltall faktum "sluttårsaker" id antallEndredeArbeidsforhold genererer ordinær og permittert og lønnsgaranti og permittertFiskeforedling avhengerAv innsendtSøknadsId,
        boolsk faktum "Permittert" id permittert,
        boolsk faktum "Ordinær" id ordinær,
        boolsk faktum "Lønnsgaranti" id lønnsgaranti,
        boolsk faktum "PermittertFiskeforedling" id permittertFiskeforedling,
        boolsk faktum "Har hatt dagpenger siste 36mnd" id harHattDagpengerSiste36mnd avhengerAv virkningsdato,
        boolsk faktum "Har brukt opp forrige dagpengeperiode" id periodeOppbruktManuell avhengerAv harHattDagpengerSiste36mnd,
        boolsk faktum "Sykepenger siste 36 mnd" id sykepengerSiste36mnd avhengerAv virkningsdato,
        boolsk faktum "Svangerskapsrelaterte sykepenger" id svangerskapsrelaterteSykepengerManuell avhengerAv sykepengerSiste36mnd,
        boolsk faktum "Fangst og fisk manuell" id fangstOgFiskManuell avhengerAv fangstOgFisk,
        boolsk faktum "Har hatt inntekt/trygdeperioder fra EØS" id eøsArbeid avhengerAv innsendtSøknadsId,
        boolsk faktum "EØS arbeid manuell" id eøsArbeidManuell avhengerAv eøsArbeid,
        boolsk faktum "Ugyldig dato manuell" id uhåndterbartVirkningsdatoManuell avhengerAv virkningsdato,
        boolsk faktum "Flere arbeidsforhold manuell" id flereArbeidsforholdManuell avhengerAv antallEndredeArbeidsforhold,
        dato faktum "Grensedato 14 dager frem i tid" id senesteMuligeVirkningsdato avhengerAv behandlingsdato,
        boolsk faktum "Oppfyller kravene til minste arbeidsinntekt, går til manuell" id oppfyllerMinsteinntektManuell,
        boolsk faktum "Har inntekt neste kalendermåned" id harInntektNesteKalendermåned avhengerAv virkningsdato,
        boolsk faktum "Har inntekt neste kalendermåned, skal til manuell" id inntektNesteKalendermånedManuell,
        UtledetFaktumFactory.Companion.min dato "Første dato av virkningsdato og behandlingsdato" id førsteAvVirkningsdatoOgBehandlingsdato av virkningsdato og behandlingsdato,
        boolsk faktum "Har mulighet til å jobbe heltid og deltid" id kanJobbeDeltid avhengerAv innsendtSøknadsId,
        boolsk faktum "Har ingen helsemessige begrensninger for arbeid" id helseTilAlleTyperJobb avhengerAv innsendtSøknadsId,
        boolsk faktum "Har mulighet til å jobbe hvor som helst" id kanJobbeHvorSomHelst avhengerAv innsendtSøknadsId,
        boolsk faktum "Er villig til å bytte yrke eller gå ned i lønn" id villigTilÅBytteYrke avhengerAv innsendtSøknadsId,
        boolsk faktum "Reell arbeidssøker manuell" id reellArbeidssøkerManuell avhengerAv kanJobbeDeltid og helseTilAlleTyperJobb og kanJobbeHvorSomHelst og villigTilÅBytteYrke,
        boolsk faktum "Registrert arbeidssøker manuell" id registrertArbeidssøkerManuell avhengerAv registrertArbeidssøkerPerioder,
        inntekt faktum "3G" id G3,
        inntekt faktum "1.5G" id G15,
        dato faktum "Fyller 67 år" id dato67år,
        boolsk faktum "oppfyllerInntekt36MånederManuell" id oppfyllerInntekt36MånederManuell,
        boolsk faktum "oppfyllerInntekt12MånederManuell" id oppfyllerInntekt12MånederManuell,
        boolsk faktum "oppfyllerVernepliktManuell" id oppfyllerVernepliktManuell,
        boolsk faktum "oppfyllerLærlingManuell" id oppfyllerLærlingManuell,
    )

    @Test
    @Disabled
    fun `intro til quiz`() {

        val prototypeWebSøknad =
            with(testSøknad) {
                Søknadprosess(
                    Seksjon(
                        "seksjon1",
                        Rolle.nav,
                    ),
                    Seksjon(
                        "manuell",
                        Rolle.manuell,
                        boolsk(oppfyllerMinsteinntektManuell),
                        boolsk(oppfyllerInntekt36MånederManuell),
                        boolsk(oppfyllerInntekt12MånederManuell),
                        boolsk(oppfyllerVernepliktManuell),
                        boolsk(oppfyllerLærlingManuell),
                    )
                )
            }

        val regeltre = with(testSøknad) {
            dato(søknadstidspunkt) før dato(dato67år) hvisGyldig {
                "minstekrav arbeidsinntekt".minstEnAv(
                    inntekt(inntektSiste36mnd) minst inntekt(G3),
                    inntekt(inntektSiste12mnd) minst inntekt(G15),
                    boolsk(verneplikt) er true,
                    boolsk(lærling) er true
                ) hvisGyldigManuell(boolsk(oppfyllerMinsteinntektManuell))
            }
        }

        val manglerInntekt = Versjon.Bygger(
            testSøknad,
            regeltre,
            mapOf(Versjon.UserInterfaceType.Web to prototypeWebSøknad)
        )
            .søknadprosess(
                Person(UUID.randomUUID(), Identer.Builder().folkeregisterIdent("12345678910").build()),
                Versjon.UserInterfaceType.Web
            )

        val graf = graph(directed = true) {
            edge["color" eq "black", Arrow.NORMAL]
            node[Color.BLACK]
            graph[Rank.dir(Rank.RankDir.TOP_TO_BOTTOM), GraphAttr.splines(GraphAttr.SplineMode.POLYLINE), GraphAttr.COMPOUND]
        }
        SubsumsjonsGraf(manglerInntekt, graf)

        val filnavn = "example/ex.png"
        graf.toGraphviz().scale(5.0).render(Format.PNG).toFile(File(filnavn))
        Runtime.getRuntime().exec("open $filnavn")
    }

    private fun regelTreMedMinstEnAv() = with(testSøknad) {
        dato(søknadstidspunkt) før dato(dato67år) hvisGyldig {
            "minstekrav arbeidsinntekt".minstEnAv(
                inntekt(inntektSiste36mnd) minst inntekt(G3),
                inntekt(inntektSiste12mnd) minst inntekt(G15),
                boolsk(verneplikt) er true,
                boolsk(lærling) er true
            ) hvisGyldigManuell(boolsk(oppfyllerMinsteinntektManuell))
        }
    }

    private fun regelTreMedDeltre() = with(testSøknad) {
        dato(søknadstidspunkt) før dato(dato67år) hvisGyldig {
            "minstekrav arbeidsinntekt".deltre {
                inntekt(inntektSiste36mnd) minst inntekt(G3) hvisUgyldig {
                    inntekt(inntektSiste12mnd) minst inntekt(G15) hvisUgyldig {
                        boolsk(verneplikt) er true hvisUgyldig {
                            boolsk(lærling) er true
                        }
                    }
                }
            } hvisGyldigManuell(boolsk(oppfyllerMinsteinntektManuell))
        }
    }

    private fun regeltreMedManuell() = with(testSøknad) {
        dato(søknadstidspunkt) før dato(dato67år) hvisGyldig {
            inntekt(inntektSiste36mnd) minst inntekt(G3) hvisUgyldig {
                inntekt(inntektSiste12mnd) minst inntekt(G15) hvisUgyldig {
                    boolsk(verneplikt) er true hvisUgyldig {
                        boolsk(lærling) er true hvisGyldigManuell(boolsk(oppfyllerLærlingManuell))
                    } hvisGyldigManuell(boolsk(oppfyllerVernepliktManuell))
                } hvisGyldigManuell(boolsk(oppfyllerInntekt12MånederManuell))
            } hvisGyldigManuell(boolsk(oppfyllerInntekt36MånederManuell))
        }
    }
}
