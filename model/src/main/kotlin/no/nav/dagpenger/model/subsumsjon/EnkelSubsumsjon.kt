package no.nav.dagpenger.model.subsumsjon

import no.nav.dagpenger.model.faktum.Fakta
import no.nav.dagpenger.model.faktum.Faktum
import no.nav.dagpenger.model.faktum.Faktum.Companion.deepCopy
import no.nav.dagpenger.model.faktum.Faktum.Companion.erAlleBesvart
import no.nav.dagpenger.model.faktum.Faktum.FaktumTilstand.Ukjent
import no.nav.dagpenger.model.faktum.GrunnleggendeFaktum
import no.nav.dagpenger.model.regel.Regel
import no.nav.dagpenger.model.seksjon.Utredningsprosess
import no.nav.dagpenger.model.visitor.SubsumsjonVisitor

open class EnkelSubsumsjon protected constructor(
    protected val regel: Regel,
    private val subsumsjonFakta: List<Faktum<*>>,
    oppfyltSubsumsjon: Subsumsjon,
    ikkeOppfyltSubsumsjon: Subsumsjon
) : Subsumsjon(regel.toString(subsumsjonFakta), oppfyltSubsumsjon, ikkeOppfyltSubsumsjon) {
    internal constructor(regel: Regel, vararg fakta: Faktum<*>) :
        this(regel, fakta.toList(), TomSubsumsjon, TomSubsumsjon)

    override fun accept(visitor: SubsumsjonVisitor) {
        resultat().also { resultat ->
            visitor.preVisit(this, regel, subsumsjonFakta, lokaltResultat(), resultat)
            subsumsjonFakta.forEach { it.accept(visitor) }
            super.accept(visitor)
            visitor.postVisit(this, regel, subsumsjonFakta, lokaltResultat(), resultat)
        }
    }

    override fun saksbehandlerForklaring() = regel.saksbehandlerForklaring(subsumsjonFakta)

    override fun deepCopy(utredningsprosess: Utredningsprosess) = deepCopy(
        regel,
        subsumsjonFakta.deepCopy(utredningsprosess),
        oppfyltSubsumsjon.deepCopy(utredningsprosess),
        ikkeOppfyltSubsumsjon.deepCopy(utredningsprosess)
    )

    override fun bygg(fakta: Fakta) = deepCopy(
        regel,
        this.subsumsjonFakta.map { fakta.id(it.faktumId) },
        oppfyltSubsumsjon.bygg(fakta),
        ikkeOppfyltSubsumsjon.bygg(fakta)
    )

    override fun deepCopy() = deepCopy(
        regel,
        subsumsjonFakta,
        oppfyltSubsumsjon.deepCopy(),
        ikkeOppfyltSubsumsjon.deepCopy()
    )

    override fun deepCopy(indeks: Int, fakta: Fakta) = deepCopy(
        regel,
        subsumsjonFakta.deepCopy(indeks, fakta),
        oppfyltSubsumsjon.deepCopy(indeks, fakta),
        ikkeOppfyltSubsumsjon.deepCopy(indeks, fakta)
    )

    private fun deepCopy(
        regel: Regel,
        fakta: List<Faktum<*>>,
        oppfyltSubsumsjon: Subsumsjon,
        ikkeOppfyltSubsumsjon: Subsumsjon
    ) = EnkelSubsumsjon(regel, fakta, oppfyltSubsumsjon, ikkeOppfyltSubsumsjon)

    override fun nesteFakta() = ukjenteFakta().takeIf { it.isNotEmpty() } ?: nesteSubsumsjon().nesteFakta()

    internal open fun ukjenteFakta(): Set<GrunnleggendeFaktum<*>> = mutableSetOf<GrunnleggendeFaktum<*>>().also {
        subsumsjonFakta.forEach { faktum -> faktum.leggTilHvis(Ukjent, it) }
    }

    private fun nesteSubsumsjon() = if (lokaltResultat() == true) oppfyltSubsumsjon else ikkeOppfyltSubsumsjon

    override fun lokaltResultat() = if (subsumsjonFakta.erAlleBesvart()) regel.resultat(subsumsjonFakta) else null

    override fun toString() = regel.toString(subsumsjonFakta)

    override operator fun get(indeks: Int) = throw IllegalArgumentException()

    override fun alleFakta(): List<Faktum<*>> = subsumsjonFakta

    override fun iterator(): Iterator<Subsumsjon> {
        return object : Iterator<Subsumsjon> {
            override fun hasNext() = false
            override fun next() = throw NoSuchElementException()
        }
    }
}
