
import guru.nidi.graphviz.attribute.Arrow
import guru.nidi.graphviz.attribute.Attributes.attr
import guru.nidi.graphviz.attribute.Color
import guru.nidi.graphviz.attribute.Font
import guru.nidi.graphviz.attribute.GraphAttr
import guru.nidi.graphviz.attribute.LinkAttr
import guru.nidi.graphviz.attribute.Rank
import guru.nidi.graphviz.attribute.Style
import guru.nidi.graphviz.engine.Format
import guru.nidi.graphviz.engine.Graphviz
import guru.nidi.graphviz.graph
import guru.nidi.graphviz.model.Compass
import guru.nidi.graphviz.model.Factory.node
import guru.nidi.graphviz.model.Graph
import guru.nidi.graphviz.model.Link
import guru.nidi.graphviz.toGraphviz
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.boolsk
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dato
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.inntekt
import no.nav.dagpenger.model.factory.UtledetFaktumFactory.Companion.maks
import no.nav.dagpenger.model.faktum.Identer
import no.nav.dagpenger.model.faktum.Person
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.marshalling.SubsumsjonsGraf
import no.nav.dagpenger.model.regel.før
import no.nav.dagpenger.model.regel.minst
import no.nav.dagpenger.model.seksjon.Seksjon
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.model.seksjon.Versjon
import no.nav.dagpenger.model.subsumsjon.alle
import no.nav.dagpenger.model.subsumsjon.hvisGyldig
import no.nav.dagpenger.model.subsumsjon.hvisUgyldig
import no.nav.dagpenger.model.subsumsjon.hvisUgyldigManuell
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.io.File
import java.util.UUID

class Graftest {

    @Test
    @Disabled
    fun test() {
        val g: Graph = guru.nidi.graphviz.model.Factory.graph("example1").directed()
            .graphAttr().with(Rank.dir(Rank.RankDir.LEFT_TO_RIGHT))
            .nodeAttr().with(Font.name("arial"))
            .linkAttr().with("class", "link-class")
            .with(
                node("hvor lang kan denne teksten være før den blir stygg?").link(
                    Link.to(node("c")).with(attr("weight", 5), Style.DASHED)
                ),
                node("a").with(Color.RED).link(node("hvor lang kan denne teksten være før den blir stygg?")),
                node("1").with(Color.RED).link(node("2")),

            )
        Graphviz.fromGraph(g).height(300).render(Format.PNG).toFile(File("example/ex1.png"))
    }

    @Test
    @Disabled
    fun test2() {
        graph(directed = true) {
            edge["color" eq "black", Arrow.NORMAL]
            node[Color.BLACK]
            graph[Rank.dir(Rank.RankDir.TOP_TO_BOTTOM), GraphAttr.splines(GraphAttr.SplineMode.POLYLINE)]

            "a" - "b" - "c"
            ("c"[Color.RED] - "d"[Color.BLUE])
            ("d" / Compass.SOUTH_WEST - "f")[LinkAttr.weight(15.0)][Color.GREEN] - "g" - "h"
            ("d" / Compass.SOUTH_EAST - "avslag")[LinkAttr.weight(10.0)][Color.RED]
            ("d" / Compass.EAST - "e" / Compass.WEST)[Color.BLACK][Arrow.NONE]
            ("d" / Compass.EAST - "e2" / Compass.WEST)[Color.BLACK][Arrow.NONE] - "e21"
            ("d" / Compass.EAST - "e3" / Compass.WEST)[Color.BLACK][Arrow.NONE] - "e31" - "e32"
        }.toGraphviz().width(1000).render(Format.PNG).toFile(File("example/ex2.png"))
        Runtime.getRuntime().exec("open example/ex2.png")
    }

    @Test
    @Disabled
    fun test3() {
        graph(directed = true) {
            edge["color" eq "black", Arrow.NORMAL]
            node[Color.BLACK]
            graph[Rank.dir(Rank.RankDir.TOP_TO_BOTTOM), GraphAttr.splines(GraphAttr.SplineMode.POLYLINE)]

            "a" - "b" - "c"
            ("c"[Color.RED] - "d"[Color.BLUE])
            ("d" / Compass.SOUTH_WEST - "f")[LinkAttr.weight(15.0)][Color.GREEN] - "g" - "h"
            ("d" / Compass.SOUTH_EAST - "avslag")[LinkAttr.weight(10.0)][Color.RED]
            ("d" / Compass.EAST - "e" / Compass.WEST)[Color.BLACK][Arrow.NONE]
            ("d" / Compass.EAST - "e2" / Compass.WEST)[Color.BLACK][Arrow.NONE] - "e21"
            ("d" / Compass.EAST - "e3" / Compass.WEST)[Color.BLACK][Arrow.NONE] - "e31" - "e32"
        }.toGraphviz().width(1000).render(Format.PNG).toFile(File("example/ex2.png"))
        Runtime.getRuntime().exec("open example/ex2.png")
    }

    private val bursdag67 = 1
    private val søknadsdato = 2
    private val ønsketdato = 3
    private val sisteDagMedLønn = 4
    private val inntektSiste3år = 5
    private val inntektSisteÅr = 6
    private val dimisjonsdato = 7
    private val virkningsdato = 8
    private val inntekt3G = 9
    private val inntekt15G = 10
    private val manuell = 11

    private val prototypeSøknad = Søknad(
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
        boolsk faktum "Manuell fordi noe" id manuell
    )

    private val prototypeWebSøknad =
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
                    inntekt(inntektSisteÅr)
                ),
                Seksjon(
                    "manuell",
                    Rolle.manuell,
                    boolsk(manuell)
                )
            )
        }

    private val prototypeSubsumsjon = with(prototypeSøknad) {
        inntekt(inntektSisteÅr) minst inntekt(inntekt15G) hvisGyldig {
            inntekt(inntektSiste3år) minst inntekt(inntekt3G) hvisUgyldigManuell (boolsk(manuell))
        } hvisUgyldig {
            dato(bursdag67) før dato(søknadsdato) hvisGyldig {
                "bursdagssjekker".alle(
                    dato(bursdag67) før dato(sisteDagMedLønn) hvisGyldig { dato(bursdag67) før dato(bursdag67) },
                    "flere sjekker".alle(
                        dato(bursdag67) før dato(dimisjonsdato),
                        inntekt(inntekt15G) minst inntekt(inntekt3G)
                    )
                )
            }
        }
    }

    @Test
    @Disabled
    fun avslag() {

        val søknadprosess = Versjon.Bygger(
            prototypeSøknad,
            prototypeSubsumsjon,
            mapOf(Versjon.UserInterfaceType.Web to prototypeWebSøknad)
        )
            .søknadprosess(
                Person(UUID.randomUUID(), Identer.Builder().folkeregisterIdent("12345678910").build()),
                Versjon.UserInterfaceType.Web
            )

        graph(directed = true) {
            edge["color" eq "black", Arrow.NORMAL]
            node[Color.BLACK]
            graph[Rank.dir(Rank.RankDir.TOP_TO_BOTTOM), GraphAttr.splines(GraphAttr.SplineMode.POLYLINE)]

            SubsumsjonsGraf(søknadprosess, this)
        }.toGraphviz().scale(10.0).render(Format.PNG).toFile(File("example/ex2.png"))
        Runtime.getRuntime().exec("open example/ex2.png")
    }
}
