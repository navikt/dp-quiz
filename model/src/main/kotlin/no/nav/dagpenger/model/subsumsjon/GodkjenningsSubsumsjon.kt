package no.nav.dagpenger.model.subsumsjon

import no.nav.dagpenger.model.faktum.Faktum
import no.nav.dagpenger.model.faktum.GrunnleggendeFaktum
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.model.visitor.SubsumsjonVisitor

open class GodkjenningsSubsumsjon private constructor(
    navn: String,
    private val action: Action,
    private val child: Subsumsjon,
    private val godkjenningsfakta: List<GrunnleggendeFaktum<Boolean>>,
    oppfyltSubsumsjon: Subsumsjon,
    ikkeOppfyltSubsumsjon: Subsumsjon
) : SammensattSubsumsjon(navn, mutableListOf(child), oppfyltSubsumsjon, ikkeOppfyltSubsumsjon) {

    init {
        godkjenningsfakta.forEach { it.godkjenner(child.alleFakta()) }
    }

    internal constructor(action: Action, child: Subsumsjon, godkjenning: GrunnleggendeFaktum<Boolean>) :
        this("${child.navn} godkjenning", action, child, listOf(godkjenning), TomSubsumsjon, TomSubsumsjon)

    internal constructor(action: Action, child: Subsumsjon, godkjenning: List<GrunnleggendeFaktum<Boolean>>) :
        this("${child.navn} godkjenning", action, child, godkjenning, TomSubsumsjon, TomSubsumsjon)

    internal constructor(navn: String, action: Action, child: Subsumsjon, godkjenning: List<GrunnleggendeFaktum<Boolean>>) :
        this(navn, action, child, godkjenning, TomSubsumsjon, TomSubsumsjon)

    enum class Action(internal val strategy: (Boolean, Boolean?) -> Boolean?) {
        JaAction({ childResultat: Boolean, godkjenningResultat: Boolean? -> childResultat && (godkjenningResultat != false) }),
        NeiAction({ childResultat: Boolean, godkjenningResultat: Boolean? -> childResultat || (godkjenningResultat == false) }),
        UansettAction({ childResultat: Boolean, godkjenningResultat: Boolean? ->
            if (godkjenningResultat != null)
                if (godkjenningResultat) childResultat else !childResultat
            else childResultat
        })
    }

    override fun lokaltResultat(): Boolean? {
        return child.resultat()?.let { childResultat ->
            action.strategy(
                childResultat,
                if (godkjenningsfakta.all { it.erBesvart() }) godkjenningsfakta.all { it.svar() } else null
            )
        }
    }

    override fun deepCopy(søknadprosess: Søknadprosess) = GodkjenningsSubsumsjon(
        navn,
        action,
        child.deepCopy(søknadprosess),
        godkjenningsfakta.map { søknadprosess.boolsk(it.id) as GrunnleggendeFaktum<Boolean> },
        oppfyltSubsumsjon.deepCopy(søknadprosess),
        ikkeOppfyltSubsumsjon.deepCopy(søknadprosess)
    )

    override fun bygg(søknad: Søknad) = GodkjenningsSubsumsjon(
        navn,
        action,
        child.bygg(søknad),
        godkjenningsfakta.map { søknad.boolsk(it.id) as GrunnleggendeFaktum<Boolean> },
        oppfyltSubsumsjon.bygg(søknad),
        ikkeOppfyltSubsumsjon.bygg(søknad)
    )

    override fun alleFakta(): List<Faktum<*>> = child.alleFakta()

    override fun deepCopy(): Subsumsjon {
        return GodkjenningsSubsumsjon(
            navn,
            action,
            child.deepCopy(),
            godkjenningsfakta,
            oppfyltSubsumsjon.deepCopy(),
            ikkeOppfyltSubsumsjon.deepCopy()
        )
    }

    override fun deepCopy(indeks: Int, søknad: Søknad): Subsumsjon {
        return GodkjenningsSubsumsjon(
            "$navn [$indeks]",
            action,
            child.deepCopy(indeks, søknad),
            godkjenningsfakta,
            oppfyltSubsumsjon.deepCopy(indeks, søknad),
            ikkeOppfyltSubsumsjon.deepCopy(indeks, søknad)
        )
    }

    override fun accept(visitor: SubsumsjonVisitor) {
        lokaltResultat().also { subsumsjon ->
            visitor.preVisit(this, action, godkjenningsfakta, subsumsjon, child.lokaltResultat())
            super.accept(visitor)
            visitor.preVisit(this, action, subsumsjon)
            godkjenningsfakta.forEach { it.accept(visitor) }
            visitor.postVisit(this, action, subsumsjon)
            visitor.postVisit(this, action, godkjenningsfakta, subsumsjon, child.lokaltResultat())
        }
    }
}
