package no.nav.dagpenger.model.marshalling

import guru.nidi.graphviz.attribute.Arrow
import guru.nidi.graphviz.attribute.Attributes.attr
import guru.nidi.graphviz.attribute.Color
import guru.nidi.graphviz.attribute.Color.GREEN
import guru.nidi.graphviz.attribute.Color.RED
import guru.nidi.graphviz.attribute.GraphAttr
import guru.nidi.graphviz.attribute.Label
import guru.nidi.graphviz.attribute.NodeAttr
import guru.nidi.graphviz.attribute.Rank
import guru.nidi.graphviz.attribute.Shape
import guru.nidi.graphviz.attribute.Style
import guru.nidi.graphviz.engine.Format
import guru.nidi.graphviz.graph
import guru.nidi.graphviz.model.Compass.SOUTH_EAST
import guru.nidi.graphviz.model.Compass.SOUTH_WEST
import guru.nidi.graphviz.model.Factory.between
import guru.nidi.graphviz.model.Factory.mutGraph
import guru.nidi.graphviz.model.Factory.node
import guru.nidi.graphviz.model.Factory.port
import guru.nidi.graphviz.model.Link
import guru.nidi.graphviz.model.MutableGraph
import guru.nidi.graphviz.model.Node
import guru.nidi.graphviz.toGraphviz
import no.nav.dagpenger.model.faktum.Faktum
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.marshalling.SubsumsjonsGraf.Kanttype.GYLDIG
import no.nav.dagpenger.model.marshalling.SubsumsjonsGraf.Kanttype.UGYLDIG
import no.nav.dagpenger.model.regel.Regel
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.model.subsumsjon.AlleSubsumsjon
import no.nav.dagpenger.model.subsumsjon.DeltreSubsumsjon
import no.nav.dagpenger.model.subsumsjon.EnkelSubsumsjon
import no.nav.dagpenger.model.subsumsjon.MinstEnAvSubsumsjon
import no.nav.dagpenger.model.subsumsjon.SammensattSubsumsjon
import no.nav.dagpenger.model.subsumsjon.Subsumsjon
import no.nav.dagpenger.model.subsumsjon.TomSubsumsjon
import no.nav.dagpenger.model.visitor.SøknadprosessVisitor
import java.io.File
import java.util.UUID

class SubsumsjonsGraf(søknadprosess: Søknadprosess) :
    SøknadprosessVisitor {

    var index = 0
    private var currentKanttype = GYLDIG

    private val opprettetAvSammensatt = mutableListOf<String>()
    private val noder = mutableListOf<Subsumsjon>()
    private val sammensattAnker = mutableListOf<String>()

    private val subGrafer = mutableListOf<MutableGraph>()

    private val rotGraf: MutableGraph = graph(directed = true) {
        edge["color" eq "black", Arrow.NORMAL]
        node[Color.BLACK]
        graph[Rank.dir(Rank.RankDir.TOP_TO_BOTTOM), GraphAttr.splines(GraphAttr.SplineMode.POLYLINE), GraphAttr.COMPOUND]
    }

    init {
        subGrafer.add(0, rotGraf)
        søknadprosess.accept(this)
    }

    fun skrivTilFil(filnavn: String) {
        rotGraf.toGraphviz().scale(5.0).render(Format.PNG).toFile(File(filnavn))
    }

    override fun preVisit(søknadprosess: Søknadprosess, uuid: UUID) {
        noder.add(søknadprosess.rootSubsumsjon)
    }

    override fun preVisit(subsumsjon: EnkelSubsumsjon, regel: Regel, fakta: List<Faktum<*>>, lokaltResultat: Boolean?, resultat: Boolean?) {
        subGrafer.first().let {
            val navnelinjer = nodeNavn(fakta, regel)
            it.add(node(subsumsjon.navn).with(Label.lines(*navnelinjer.toTypedArray())))

            if (noder.first() == subsumsjon) return

            if (subsumsjon.navn in opprettetAvSammensatt) {
                noder.add(0, subsumsjon)
                return
            }

            val tilNode = if (fakta.any { faktum -> faktum.harRolle(Rolle.manuell) }) {
                node(subsumsjon.navn).with(RED, RED.font())
            } else node(subsumsjon.navn)

            if(noder.first() is SammensattSubsumsjon) sammensattTilEnkel(tilNode)
            else enkelTilEnkel(tilNode)
        }
        noder.add(0, subsumsjon)
    }

    private fun enkelTilEnkel(tilNode: Node) {
        subGrafer.first().add(
            node(noder.first().navn).link(
                between(
                    port(kantRetning()),
                    tilNode
                ).withUtfallAttrs()
            )
        )
    }

    private fun sammensattTilEnkel(tilNode: Node) {
        subGrafer.first().add(
            node(sammensattAnker.first()).link(
                between(
                    port(kantRetning()),
                    tilNode
                ).withUtfallAttrs().with(attr("ltail", "cluster_${noder.first().navn}"))
            )
        )
    }

    private fun nodeNavn(fakta: List<Faktum<*>>, regel: Regel): List<String> {
        if (fakta.any { it.harRolle(Rolle.manuell) }) return listOf("Manuell behandling")
        if (fakta.size == 1) return listOf(regel.kortNavn(fakta))
        return listOf(fakta[0].navn, regel.typeNavn, fakta[1].navn)
    }

    override fun postVisit(subsumsjon: EnkelSubsumsjon, regel: Regel, fakta: List<Faktum<*>>, lokaltResultat: Boolean?, resultat: Boolean?) {
        noder.removeFirst()
    }

    override fun preVisit(subsumsjon: AlleSubsumsjon, subsumsjoner: List<Subsumsjon>, lokaltResultat: Boolean?, resultat: Boolean?) {
        lagSammensattNode(subsumsjon, subsumsjoner, "Alle")
    }

    override fun postVisit(subsumsjon: AlleSubsumsjon, subsumsjoner: List<Subsumsjon>, lokaltResultat: Boolean?, resultat: Boolean?) {
        ryddSammensattNode()
    }

    override fun preVisit(subsumsjon: MinstEnAvSubsumsjon, subsumsjoner: List<Subsumsjon>, lokaltResultat: Boolean?, resultat: Boolean?) {
        lagSammensattNode(subsumsjon, subsumsjoner, "Minst en av")
    }

    override fun postVisit(subsumsjon: MinstEnAvSubsumsjon, subsumsjoner: List<Subsumsjon>, lokaltResultat: Boolean?, resultat: Boolean?) {
        ryddSammensattNode()
    }

    override fun preVisit(subsumsjon: DeltreSubsumsjon, child: Subsumsjon, lokaltResultat: Boolean?, resultat: Boolean?) {
        lagSammensattNode(subsumsjon, listOf(child), "Deltre")
    }

    override fun postVisit(subsumsjon: DeltreSubsumsjon, child: Subsumsjon, lokaltResultat: Boolean?, resultat: Boolean?) {
        ryddSammensattNode()
    }

    override fun preVisitGyldig(parent: Subsumsjon, child: Subsumsjon) {
        if (parent is SammensattSubsumsjon) {
            // Under-subsumsjonene har blitt behandlet, og gyldig/ugyldig-treene skal ikke være med i boksen
            val subGraf = subGrafer.removeFirst()
            subGraf.addTo(subGrafer.first())
        }
        kant(parent, child, GYLDIG)
    }

    override fun preVisitUgyldig(parent: Subsumsjon, child: Subsumsjon) {
        kant(parent, child, UGYLDIG)
    }

    private fun kant(parent: Subsumsjon, child: Subsumsjon, kanttype: Kanttype) {
        currentKanttype = kanttype

        val erManuell = parent.alleFakta().any { it.harRolle(Rolle.manuell) }

        if (child is TomSubsumsjon && !erManuell && subGrafer.size == 1) {
            if (parent is SammensattSubsumsjon) {
                utfallFraSammensatt(parent)
            } else {
                utfallFraEnkel(parent)
            }
            index++
        }
    }

    private fun utfallFraEnkel(parent: Subsumsjon) {
        subGrafer.first().add(
            node(parent.navn).link(
                between(
                    port(kantRetning()),
                    node("utfall$index").withUtfallAttrs()
                ).withUtfallAttrs()
            )
        )
    }

    private fun utfallFraSammensatt(parent: Subsumsjon) {
        subGrafer.first().add(
            node(sammensattAnker.first()).link(
                between(
                    port(kantRetning()),
                    node("utfall$index").withUtfallAttrs()
                ).withUtfallAttrs().with(attr("ltail", "cluster_${parent.navn}"))
            )
        )
    }

    private fun Link.withUtfallAttrs() = this.with(kantFarge(), attr("weight", 10))

    private fun Node.withUtfallAttrs(): Node {
        val label = when (currentKanttype) {
            GYLDIG -> "Innvilget"
            UGYLDIG -> "Avslag"
        }
        return this.with(kantFarge().font(), kantFarge(), Label.lines(label))
    }

    private fun lagSammensattNode(subsumsjon: SammensattSubsumsjon, subsumsjoner: List<Subsumsjon>, label: String) {

        subGrafer.first().let {
            val nodeLabel = Label.lines(label, subsumsjon.navn)

            val subgraf =
                mutGraph().setCluster(true).setDirected(true).setName(subsumsjon.navn)
                    .graphAttrs().add(Rank.dir(Rank.RankDir.TOP_TO_BOTTOM), GraphAttr.COMPOUND, nodeLabel)
                    .graphAttrs().add(nodeLabel)

            sammensattAnker.add(0, "${subsumsjon.navn}_dummy")

            subgraf.add(node(sammensattAnker.first()).with(Style.INVIS, Shape.NONE, Label.of("")))

            if (subsumsjon.navn !in opprettetAvSammensatt && noder.first().navn != subsumsjon.navn) {
                it.add(
                    node(noder.first().navn).link(
                        between(
                            port(kantRetning()),
                            node(sammensattAnker.first())
                        ).withUtfallAttrs().with(attr("lhead", "cluster_${subsumsjon.navn}"), attr("minlen", 2))
                    )
                )
            }

            subsumsjoner.forEach { child ->
                if(child !is SammensattSubsumsjon) subgraf.add(node(child.navn))
                opprettetAvSammensatt.add(child.navn)
            }

            subGrafer.add(0, subgraf)
        }

        noder.add(0, subsumsjon)
    }

    private fun ryddSammensattNode() {
        noder.removeFirst()
        sammensattAnker.removeFirst()
    }

    private fun kantRetning() = when (currentKanttype) {
        GYLDIG -> SOUTH_WEST
        UGYLDIG -> SOUTH_EAST
    }

    private fun kantFarge() = when (currentKanttype) {
        GYLDIG -> GREEN
        UGYLDIG -> RED
    }

    private enum class Kanttype {
        GYLDIG, UGYLDIG
    }
}
