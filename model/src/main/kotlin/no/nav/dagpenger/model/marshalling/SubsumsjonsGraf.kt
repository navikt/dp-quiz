package no.nav.dagpenger.model.marshalling

import guru.nidi.graphviz.KraphvizContext
import guru.nidi.graphviz.attribute.Arrow
import guru.nidi.graphviz.attribute.Color.BLACK
import guru.nidi.graphviz.attribute.Color.GREEN
import guru.nidi.graphviz.attribute.Color.RED
import guru.nidi.graphviz.attribute.Label
import guru.nidi.graphviz.attribute.Style
import guru.nidi.graphviz.model.Compass.EAST
import guru.nidi.graphviz.model.Compass.SOUTH_EAST
import guru.nidi.graphviz.model.Compass.SOUTH_WEST
import no.nav.dagpenger.model.faktum.Faktum
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.marshalling.SubsumsjonsGraf.KantTyper.GYLDIG
import no.nav.dagpenger.model.marshalling.SubsumsjonsGraf.KantTyper.UGYLDIG
import no.nav.dagpenger.model.regel.Regel
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.model.subsumsjon.AlleSubsumsjon
import no.nav.dagpenger.model.subsumsjon.EnkelSubsumsjon
import no.nav.dagpenger.model.subsumsjon.MinstEnAvSubsumsjon
import no.nav.dagpenger.model.subsumsjon.SammensattSubsumsjon
import no.nav.dagpenger.model.subsumsjon.Subsumsjon
import no.nav.dagpenger.model.subsumsjon.TomSubsumsjon
import no.nav.dagpenger.model.visitor.SøknadprosessVisitor
import java.util.UUID

class SubsumsjonsGraf(søknadprosess: Søknadprosess, private val kraphvizContext: KraphvizContext) :
    SøknadprosessVisitor {

    var index = 0
    private var kantType = GYLDIG
    private var løvnode = false

    private val sammensattNoder = mutableListOf<String>()
    private val noder = mutableListOf<String>("rot")

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
            val label = listOf(fakta[0].navn, regel.typeNavn) + if (fakta.size == 2) listOf(fakta[1].navn) else listOf()
            subsumsjon.navn[Label.lines(*label.toTypedArray())]

            if (subsumsjon.navn in sammensattNoder) {
                noder.add(0, subsumsjon.navn)
                return
            }

            if (fakta.any { it.harRolle(Rolle.manuell) }) {
                løvnode = true
                (noder.first() / kantRetning() - subsumsjon.navn[Label.lines("Manuell behandling")][RED][RED.font()])[kantFarge()][kantType()]
            } else {
                (noder.first() / kantRetning() - subsumsjon.navn)[kantFarge()][kantType()]
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
        løvnode = false
    }

    override fun preVisit(subsumsjon: AlleSubsumsjon, subsumsjoner: List<Subsumsjon>, lokaltResultat: Boolean?, resultat: Boolean?) {
        sammensattNode(subsumsjon, subsumsjoner, "Alle")
    }

    override fun postVisit(subsumsjon: AlleSubsumsjon, subsumsjoner: List<Subsumsjon>, lokaltResultat: Boolean?, resultat: Boolean?) {
        noder.removeFirst()
    }

    override fun preVisit(subsumsjon: MinstEnAvSubsumsjon, subsumsjoner: List<Subsumsjon>, lokaltResultat: Boolean?, resultat: Boolean?) {
        sammensattNode(subsumsjon, subsumsjoner, "Minst en av")
    }

    override fun postVisit(subsumsjon: MinstEnAvSubsumsjon, subsumsjoner: List<Subsumsjon>, lokaltResultat: Boolean?, resultat: Boolean?) {
        noder.removeFirst()
    }

    override fun preVisitGyldig(parent: Subsumsjon, child: Subsumsjon) {
        if (child is TomSubsumsjon && !løvnode) {
            with(kraphvizContext) {
                (parent.navn - "Innvilget$index"[GREEN][GREEN.font()][Label.lines("Innvilget")])[GREEN]
                index++
            }
        }
        kantType = GYLDIG
    }

    override fun preVisitUgyldig(parent: Subsumsjon, child: Subsumsjon) {
        if (child is TomSubsumsjon && !løvnode) {
            with(kraphvizContext) {
                (parent.navn - "Avslag$index"[RED][RED.font()][Label.lines("Avslag")])[RED]
                index++
            }
        }
        kantType = UGYLDIG
    }

    private fun sammensattNode(subsumsjon: SammensattSubsumsjon, subsumsjoner: List<Subsumsjon>, label: String) {
        if (subsumsjon.navn !in sammensattNoder) {
            with(kraphvizContext) {
                (noder.first() / kantRetning() - subsumsjon.navn)[kantFarge()][kantType()]
            }
        }

        with(kraphvizContext) {
            subsumsjon.navn[Label.lines(label, subsumsjon.navn)]

            subsumsjoner.forEach {
                (subsumsjon.navn / EAST - it.navn)[BLACK][Arrow.NONE][Style.DASHED]
                sammensattNoder.add(it.navn)
            }
        }
        noder.add(0, subsumsjon.navn)
    }

    private fun kantRetning() = when (kantType) {
        GYLDIG -> SOUTH_WEST
        UGYLDIG -> SOUTH_EAST
    }

    private fun kantFarge() = when (kantType) {
        GYLDIG -> GREEN
        UGYLDIG -> RED
    }

    private fun kantType() = when (kantType) {
        GYLDIG -> Arrow.NORMAL
        UGYLDIG -> Arrow.NORMAL
    }

    private enum class KantTyper {
        GYLDIG, UGYLDIG
    }
}
