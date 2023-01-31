package no.nav.dagpenger.model.subsumsjon

import no.nav.dagpenger.model.faktum.Faktum
import no.nav.dagpenger.model.faktum.Faktum.Companion.deepCopy
import no.nav.dagpenger.model.faktum.Faktum.Companion.erAlleBesvart
import no.nav.dagpenger.model.faktum.Faktum.FaktumTilstand.Ukjent
import no.nav.dagpenger.model.faktum.GrunnleggendeFaktum
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.regel.Regel
import no.nav.dagpenger.model.seksjon.Faktagrupper
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

    override fun deepCopy(faktagrupper: Faktagrupper) = deepCopy(
        regel,
        subsumsjonFakta.deepCopy(faktagrupper),
        oppfyltSubsumsjon.deepCopy(faktagrupper),
        ikkeOppfyltSubsumsjon.deepCopy(faktagrupper)
    )

    override fun bygg(søknad: Søknad) = deepCopy(
        regel,
        this.subsumsjonFakta.map { søknad.id(it.faktumId) },
        oppfyltSubsumsjon.bygg(søknad),
        ikkeOppfyltSubsumsjon.bygg(søknad)
    )

    override fun deepCopy() = deepCopy(
        regel,
        subsumsjonFakta,
        oppfyltSubsumsjon.deepCopy(),
        ikkeOppfyltSubsumsjon.deepCopy()
    )

    override fun deepCopy(indeks: Int, søknad: Søknad) = deepCopy(
        regel,
        subsumsjonFakta.deepCopy(indeks, søknad),
        oppfyltSubsumsjon.deepCopy(indeks, søknad),
        ikkeOppfyltSubsumsjon.deepCopy(indeks, søknad)
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
