package no.nav.dagpenger.model.marshalling

import guru.nidi.graphviz.KraphvizContext
import guru.nidi.graphviz.attribute.Arrow
import guru.nidi.graphviz.attribute.Attributes.attr
import guru.nidi.graphviz.attribute.Color.GREEN
import guru.nidi.graphviz.attribute.Color.RED
import guru.nidi.graphviz.attribute.Label
import guru.nidi.graphviz.attribute.LinkAttr
import guru.nidi.graphviz.graph
import guru.nidi.graphviz.model.Compass
import guru.nidi.graphviz.model.Compass.EAST
import guru.nidi.graphviz.model.Compass.SOUTH_EAST
import guru.nidi.graphviz.model.Compass.SOUTH_WEST
import guru.nidi.graphviz.model.Compass.WEST
import no.nav.dagpenger.model.faktum.Faktum
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.marshalling.SubsumsjonsGraf.Kanttype.GYLDIG
import no.nav.dagpenger.model.marshalling.SubsumsjonsGraf.Kanttype.UGYLDIG
import no.nav.dagpenger.model.regel.Regel
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.model.subsumsjon.AlleSubsumsjon
import no.nav.dagpenger.model.subsumsjon.EnkelSubsumsjon
import no.nav.dagpenger.model.subsumsjon.MinstEnAvSubsumsjon
import no.nav.dagpenger.model.subsumsjon.SammensattSubsumsjon
import no.nav.dagpenger.model.subsumsjon.Subsumsjon
import no.nav.dagpenger.model.visitor.SøknadprosessVisitor
import java.util.UUID

class SubsumsjonsGraf(søknadprosess: Søknadprosess, private val kraphvizContext: KraphvizContext) :
    SøknadprosessVisitor {

    var index = 0
    private var currentKanttype = GYLDIG

    private val opprettetAvSammensatt = mutableListOf<String>()
    private val noder = mutableListOf<String>("rot")
    private val iSammensattSubtre = mutableListOf<String>("Enkel")

    private val grafContext = mutableListOf(kraphvizContext)

    init {
        søknadprosess.accept(this)
    }

    override fun preVisit(søknadprosess: Søknadprosess, uuid: UUID) {
        noder.add(søknadprosess.rootSubsumsjon.navn)
    }

    override fun preVisit(
        subsumsjon: EnkelSubsumsjon,
        regel: Regel,
        fakta: List<Faktum<*>>,
        lokaltResultat: Boolean?,
        resultat: Boolean?
    ) {
        // if(noder.first() == subsumsjon.navn) return

        with(kraphvizContext) {
            val navnelinjer = listOf(fakta[0].navn, regel.typeNavn) + if (fakta.size == 2) listOf(fakta[1].navn) else listOf()
            subsumsjon.navn[Label.lines(*navnelinjer.toTypedArray())]

            if (subsumsjon.navn in opprettetAvSammensatt) {
                noder.add(0, subsumsjon.navn)
                return
            }

            if (fakta.any { it.harRolle(Rolle.manuell) }) {
                (noder.first() / kantRetning() - subsumsjon.navn[Label.lines("Manuell behandling")][RED][RED.font()])[kantFarge()][kantType()][LinkAttr.weight(10.0)]
            } else {
                (noder.first() / kantRetning() - subsumsjon.navn)[kantFarge()][kantType()][LinkAttr.weight(10.0)]
            }
        }

        noder.add(0, subsumsjon.navn)
    }

    override fun postVisit(
        subsumsjon: EnkelSubsumsjon,
        regel: Regel,
        fakta: List<Faktum<*>>,
        lokaltResultat: Boolean?,
        resultat: Boolean?
    ) {
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

    override fun preVisitGyldig(parent: Subsumsjon, child: Subsumsjon) {
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

        /*
        if (child is TomSubsumsjon && !erManuell) {
            with(kraphvizContext) {
                (parent.navn - "$label$index"[kantFarge()][kantFarge().font()][Label.lines(label)])[kantFarge()]
                index++
            }
        }
         */
    }

    private fun lagSammensattNode(subsumsjon: SammensattSubsumsjon, subsumsjoner: List<Subsumsjon>, label: String) {
        with(kraphvizContext) {
            if (subsumsjon.navn !in opprettetAvSammensatt) {
                (noder.first() / kantRetning() - subsumsjon.navn)[kantFarge()][kantType()][LinkAttr.weight(10.0)]
            }
            subsumsjon.navn[Label.lines(label, subsumsjon.navn)]

            graph(directed = true, cluster = true, name = subsumsjon.navn) {
                subsumsjoner.forEach {
                    val retning = if (subsumsjon is AlleSubsumsjon) WEST else EAST
                    val retningMotsatt = if (subsumsjon is AlleSubsumsjon) EAST else WEST
                    it.navn / Compass.NORTH
                    opprettetAvSammensatt.add(it.navn)
                }
            }.graphAttrs().add(Label.lines("$label ${subsumsjon.navn}"))
            (subsumsjon.navn - subsumsjoner.first().navn)[attr("lhead", "cluster_${subsumsjon.navn}")]
        }
        noder.add(0, subsumsjon.navn)
        iSammensattSubtre.add(0, "Sammensatt")
    }

    private fun ryddSammensattNode() {
        noder.removeFirst()
        iSammensattSubtre.removeFirst()
    }

    private fun kantRetning() = when (currentKanttype) {
        GYLDIG -> SOUTH_WEST
        UGYLDIG -> SOUTH_EAST
    }

    private fun kantFarge() = when (currentKanttype) {
        GYLDIG -> GREEN
        UGYLDIG -> RED
    }

    private fun kantType() = when (currentKanttype) {
        GYLDIG -> Arrow.NORMAL
        UGYLDIG -> Arrow.NORMAL
    }

    private enum class Kanttype {
        GYLDIG, UGYLDIG
    }
}
