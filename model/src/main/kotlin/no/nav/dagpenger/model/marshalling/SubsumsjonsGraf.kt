package no.nav.dagpenger.model.marshalling

import guru.nidi.graphviz.KraphvizContext
import guru.nidi.graphviz.attribute.Arrow
import guru.nidi.graphviz.attribute.Color
import guru.nidi.graphviz.attribute.Color.BLACK
import guru.nidi.graphviz.attribute.Color.GREEN
import guru.nidi.graphviz.attribute.Color.RED
import guru.nidi.graphviz.attribute.Label
import guru.nidi.graphviz.model.Compass.EAST
import guru.nidi.graphviz.model.Compass.SOUTH
import guru.nidi.graphviz.model.Compass.SOUTH_EAST
import guru.nidi.graphviz.model.Compass.SOUTH_WEST
import no.nav.dagpenger.model.faktum.Faktum
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.marshalling.SubsumsjonsGraf.KantTyper.GYLDIG
import no.nav.dagpenger.model.marshalling.SubsumsjonsGraf.KantTyper.SAMMENSATT
import no.nav.dagpenger.model.marshalling.SubsumsjonsGraf.KantTyper.UGYLDIG
import no.nav.dagpenger.model.regel.Regel
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.model.subsumsjon.AlleSubsumsjon
import no.nav.dagpenger.model.subsumsjon.EnkelSubsumsjon
import no.nav.dagpenger.model.subsumsjon.MinstEnAvSubsumsjon
import no.nav.dagpenger.model.subsumsjon.Subsumsjon
import no.nav.dagpenger.model.subsumsjon.TomSubsumsjon
import no.nav.dagpenger.model.visitor.SøknadprosessVisitor
import java.util.UUID

class SubsumsjonsGraf(søknadprosess: Søknadprosess, private val kraphvizContext: KraphvizContext) :
    SøknadprosessVisitor {
    var forrigeNode = "rot"
    var steps = 0
    private var kantType = GYLDIG
    private var løvnode = false

    private enum class KantTyper {
        GYLDIG, UGYLDIG, SAMMENSATT
    }

    private var iSammensatt = false

    val noder = mutableListOf<String>("rot")

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
        // if(noder.first() == subsumsjon.toString()) return

        with(kraphvizContext) {

            if (fakta.any { it.harRolle(Rolle.manuell) }) {
                løvnode = true
                (noder.first() / kantRetning() - subsumsjon.navn[Label.lines("Manuell behandling")][RED][RED.font()])[kantFarge()][kantType()]
            } else {
                val label = listOf(fakta[0].navn, regel.typeNavn) + if (fakta.size == 2) listOf(fakta[1].navn) else listOf()
                (noder.first() / kantRetning() - subsumsjon.navn[Label.lines(*label.toTypedArray())])[kantFarge()][kantType()]
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

    override fun preVisit(subsumsjon: AlleSubsumsjon, lokaltResultat: Boolean?, resultat: Boolean?) {
        with(kraphvizContext) {
            (noder.first() / kantRetning() - subsumsjon.navn[Label.lines("Alle", subsumsjon.navn)])[kantFarge()][kantType()]
        }
        noder.add(0, subsumsjon.navn)
        kantType = SAMMENSATT
        iSammensatt = true
    }

    override fun postVisit(subsumsjon: AlleSubsumsjon, lokaltResultat: Boolean?, resultat: Boolean?) {
        noder.removeFirst()
        iSammensatt = false
    }

    override fun preVisit(subsumsjon: MinstEnAvSubsumsjon, lokaltResultat: Boolean?, resultat: Boolean?) {
        with(kraphvizContext) {
            (noder.first() / kantRetning() - subsumsjon.navn[Label.lines("Minst en", subsumsjon.navn)])[kantFarge()][kantType()]
        }
        noder.add(0, subsumsjon.navn)
        kantType = SAMMENSATT
        iSammensatt = true
    }

    override fun postVisit(subsumsjon: MinstEnAvSubsumsjon, lokaltResultat: Boolean?, resultat: Boolean?) {
        noder.removeFirst()
        iSammensatt = false
    }

    override fun preVisitGyldig(parent: Subsumsjon, child: Subsumsjon) {
        if (child is TomSubsumsjon && !løvnode) {
            with(kraphvizContext) {
                (parent.navn - "Innvilget$steps"[GREEN][GREEN.font()][Label.lines("Innvilget")])[GREEN]
                steps++
            }
        }
        kantType = GYLDIG
    }

    override fun preVisitUgyldig(parent: Subsumsjon, child: Subsumsjon) {
        if (child is TomSubsumsjon && !løvnode) {
            with(kraphvizContext) {
                (parent.navn - "Avslag$steps"[RED][RED.font()][Label.lines("Avslag")])[RED]
                steps++
            }
        }
        kantType = UGYLDIG
    }

    private fun kantRetning() = when {
        iSammensatt -> EAST
        kantType == GYLDIG -> SOUTH_WEST
        kantType == UGYLDIG -> SOUTH_EAST
        else -> SOUTH
    }

    private fun kantFarge() = when {
        iSammensatt -> BLACK
        kantType == GYLDIG -> GREEN
        kantType == UGYLDIG -> RED
        else -> Color.DARKVIOLET
    }

    private fun kantType() = when {
        iSammensatt -> Arrow.NONE
        kantType == GYLDIG -> Arrow.NORMAL
        kantType == UGYLDIG -> Arrow.NORMAL
        else -> Arrow.TEE
    }
}
