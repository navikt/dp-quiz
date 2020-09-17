package no.nav.dagpenger.model.visitor

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import no.nav.dagpenger.model.fakta.Faktum
import no.nav.dagpenger.model.fakta.GrunnleggendeFaktum
import no.nav.dagpenger.model.fakta.UtledetFaktum
import no.nav.dagpenger.model.regel.Regel
import no.nav.dagpenger.model.subsumsjon.AlleSubsumsjon
import no.nav.dagpenger.model.subsumsjon.EnkelSubsumsjon
import no.nav.dagpenger.model.subsumsjon.MinstEnAvSubsumsjon
import no.nav.dagpenger.model.subsumsjon.Subsumsjon

class JsonBuilder(private val subsumsjon: Subsumsjon) : SubsumsjonVisitor {
    private val mapper = ObjectMapper()
    private var root: ObjectNode = mapper.createObjectNode()

    init {
        subsumsjon.accept(this)
    }

    override fun preVisit(subsumsjon: EnkelSubsumsjon, regel: Regel) {
        super.preVisit(subsumsjon, regel)
    }

    override fun preVisit(subsumsjon: AlleSubsumsjon) {
        super.preVisit(subsumsjon)
    }

    override fun preVisit(subsumsjon: MinstEnAvSubsumsjon) {
        super.preVisit(subsumsjon)
    }

    override fun <R : Comparable<R>> preVisit(faktum: UtledetFaktum<R>, id: Int, svar: R) {
        super.preVisit(faktum, id, svar)
    }

    override fun <R : Comparable<R>> preVisit(faktum: UtledetFaktum<R>, id: Int) {
        super.preVisit(faktum, id)
    }

    override fun <R : Comparable<R>> preVisit(fakta: Set<Faktum<R>>) {
        super.preVisit(fakta)
    }

    override fun postVisit(subsumsjon: EnkelSubsumsjon, regel: Regel) {
        super.postVisit(subsumsjon, regel)
    }

    override fun postVisit(subsumsjon: AlleSubsumsjon) {
        super.postVisit(subsumsjon)
    }

    override fun postVisit(subsumsjon: MinstEnAvSubsumsjon) {
        super.postVisit(subsumsjon)
    }

    override fun <R : Comparable<R>> postVisit(faktum: UtledetFaktum<R>, id: Int) {
        super.postVisit(faktum, id)
    }

    override fun <R : Comparable<R>> postVisit(fakta: Set<Faktum<R>>) {
        super.postVisit(fakta)
    }

    override fun preVisitGyldig(parent: Subsumsjon, child: Subsumsjon) {
        super.preVisitGyldig(parent, child)
    }

    override fun postVisitGyldig(parent: Subsumsjon, child: Subsumsjon) {
        super.postVisitGyldig(parent, child)
    }

    override fun preVisitUgyldig(parent: Subsumsjon, child: Subsumsjon) {
        super.preVisitUgyldig(parent, child)
    }

    override fun postVisitUgyldig(parent: Subsumsjon, child: Subsumsjon) {
        super.postVisitUgyldig(parent, child)
    }

    override fun <R : Comparable<R>> visit(faktum: GrunnleggendeFaktum<R>, tilstand: Faktum.FaktumTilstand, id: Int) {
        mapper.createObjectNode().also {
            it.put("navn", faktum.navn.toString())
            it.put("id", id)
            root = it
        }
    }

    override fun <R : Comparable<R>> visit(faktum: GrunnleggendeFaktum<R>, tilstand: Faktum.FaktumTilstand, id: Int, svar: R) {
        super.visit(faktum, tilstand, id, svar)
    }

    fun resultat() = root
}
