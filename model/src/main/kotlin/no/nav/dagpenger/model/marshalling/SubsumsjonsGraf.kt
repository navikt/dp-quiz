package no.nav.dagpenger.model.marshalling

import guru.nidi.graphviz.attribute.Arrow
import guru.nidi.graphviz.attribute.Attributes.attr
import guru.nidi.graphviz.attribute.Color
import guru.nidi.graphviz.attribute.Color.GREEN
import guru.nidi.graphviz.attribute.Color.RED
import guru.nidi.graphviz.attribute.GraphAttr
import guru.nidi.graphviz.attribute.Label
import guru.nidi.graphviz.attribute.Rank
import guru.nidi.graphviz.engine.Format
import guru.nidi.graphviz.graph
import guru.nidi.graphviz.model.Compass.EAST
import guru.nidi.graphviz.model.Compass.SOUTH_EAST
import guru.nidi.graphviz.model.Compass.SOUTH_WEST
import guru.nidi.graphviz.model.Factory.between
import guru.nidi.graphviz.model.Factory.mutGraph
import guru.nidi.graphviz.model.Factory.node
import guru.nidi.graphviz.model.Factory.port
import guru.nidi.graphviz.model.MutableGraph
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
    private val noder = mutableListOf<String>()
    private val sammensattAnker = mutableListOf<Subsumsjon>()

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
        noder.add(søknadprosess.rootSubsumsjon.navn)
    }

    override fun preVisit(subsumsjon: EnkelSubsumsjon, regel: Regel, fakta: List<Faktum<*>>, lokaltResultat: Boolean?, resultat: Boolean?) {
        subGrafer.first().let {
            val navnelinjer = nodeNavn(fakta, regel)
            it.add(node(subsumsjon.navn).with(Label.lines(*navnelinjer.toTypedArray())))

            if (noder.first() == subsumsjon.navn) return

            if (subsumsjon.navn in opprettetAvSammensatt) {
                noder.add(0, subsumsjon.navn)
                return
            }

            val tilNode = if (fakta.any { faktum -> faktum.harRolle(Rolle.manuell) }) {
                node(subsumsjon.navn).with(RED, RED.font())
            } else node(subsumsjon.navn)

            it.add(
                node(noder.first()).link(
                    between(port(kantRetning()), tilNode)
                        .with(kantFarge(), attr("weight", 10))
                )
            )
        }
        noder.add(0, subsumsjon.navn)
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
        val label = when (kanttype) {
            GYLDIG -> "Innvilget"
            UGYLDIG -> "Avslag"
        }
        currentKanttype = kanttype

        val erManuell = parent.alleFakta().any { it.harRolle(Rolle.manuell) }

        if (child is TomSubsumsjon && !erManuell && subGrafer.size == 1) {
            subGrafer.first().let {
                val fra =
                    if(parent is SammensattSubsumsjon) node(sammensattAnker.first().navn)
                    else node(parent.navn)

                val linkAttrs = mutableListOf(kantFarge(), attr("weight", 10))
                if(parent is SammensattSubsumsjon) linkAttrs.add(attr("ltail", "cluster_${parent.navn}"))

                it.add(
                    fra.link(
                        between(port(kantRetning()), node("$label$index").with(kantFarge().font(), kantFarge(), Label.lines(label)))
                            .with(*linkAttrs.toTypedArray())
                    )
                )
                index++
            }
        }
    }

    private fun lagSammensattNode(subsumsjon: SammensattSubsumsjon, subsumsjoner: List<Subsumsjon>, label: String) {

        subGrafer.first().let {
            val nodeLabel = Label.lines(label, subsumsjon.navn)
            rotGraf.add(node(subsumsjon.navn).with(nodeLabel))

            if (subsumsjon.navn !in opprettetAvSammensatt && noder.first() != subsumsjon.navn) {
                it.add(
                    node(noder.first()).link(
                        between(port(kantRetning()), node(subsumsjon.navn))
                            .with(kantFarge(), attr("weight", 10))
                    )
                )
            }

            val subgraf =
                mutGraph().setCluster(true).setDirected(true).setName(subsumsjon.navn)
                    .graphAttrs().add(Rank.dir(Rank.RankDir.TOP_TO_BOTTOM), GraphAttr.COMPOUND, nodeLabel)
                    .graphAttrs().add(nodeLabel)

            subsumsjoner.forEach { child ->
                subgraf.add(node(child.navn))
                opprettetAvSammensatt.add(child.navn)
            }

            sammensattAnker.add(0, subsumsjoner.first())

            it.add(
                node(subsumsjon.navn).link(
                    between(port(EAST), node(sammensattAnker.first().navn))
                        .with(attr("lhead", "cluster_${subsumsjon.navn}"))
                )
            )
            subGrafer.add(0, subgraf)
        }

        noder.add(0, subsumsjon.navn)
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
