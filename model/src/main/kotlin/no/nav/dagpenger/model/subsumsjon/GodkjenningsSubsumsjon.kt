package no.nav.dagpenger.model.subsumsjon

import no.nav.dagpenger.model.faktum.Fakta
import no.nav.dagpenger.model.faktum.Faktum
import no.nav.dagpenger.model.faktum.Faktum.Companion.deepCopy
import no.nav.dagpenger.model.faktum.GrunnleggendeFaktum
import no.nav.dagpenger.model.seksjon.Prosess
import no.nav.dagpenger.model.visitor.SubsumsjonVisitor

open class GodkjenningsSubsumsjon private constructor(
    navn: String,
    private val action: Action,
    private val child: Subsumsjon,
    private val godkjenningsfakta: List<GrunnleggendeFaktum<Boolean>>,
    oppfyltSubsumsjon: Subsumsjon,
    ikkeOppfyltSubsumsjon: Subsumsjon,
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

    enum class Action(
        internal val strategy: (Boolean, Boolean?) -> Boolean?,
    ) {
        JaAction({ childResultat: Boolean, godkjenningResultat: Boolean? -> childResultat && (godkjenningResultat != false) }),
        NeiAction({ childResultat: Boolean, godkjenningResultat: Boolean? -> childResultat || (godkjenningResultat == false) }),
        UansettAction({ childResultat: Boolean, godkjenningResultat: Boolean? ->
            if (godkjenningResultat != null) {
                if (godkjenningResultat) {
                    childResultat
                } else {
                    !childResultat
                }
            } else {
                childResultat
            }
        }),
    }

    override fun lokaltResultat(): Boolean? =
        child.resultat()?.let { childResultat ->
            action.strategy(
                childResultat,
                if (godkjenningsfakta.all { it.erBesvart() }) godkjenningsfakta.all { it.svar() } else null,
            )
        }

    override fun deepCopy(prosess: Prosess) =
        GodkjenningsSubsumsjon(
            navn,
            action,
            child.deepCopy(prosess),
            godkjenningsfakta.map { prosess.boolsk(it.id) as GrunnleggendeFaktum<Boolean> },
            oppfyltSubsumsjon.deepCopy(prosess),
            ikkeOppfyltSubsumsjon.deepCopy(prosess),
        )

    override fun bygg(fakta: Fakta) =
        GodkjenningsSubsumsjon(
            navn,
            action,
            child.bygg(fakta),
            godkjenningsfakta.map { fakta.boolsk(it.id) as GrunnleggendeFaktum<Boolean> },
            oppfyltSubsumsjon.bygg(fakta),
            ikkeOppfyltSubsumsjon.bygg(fakta),
        )

    override fun alleFakta(): List<Faktum<*>> = child.alleFakta()

    override fun deepCopy(): Subsumsjon =
        GodkjenningsSubsumsjon(
            navn,
            action,
            child.deepCopy(),
            godkjenningsfakta,
            oppfyltSubsumsjon.deepCopy(),
            ikkeOppfyltSubsumsjon.deepCopy(),
        )

    override fun deepCopy(
        indeks: Int,
        fakta: Fakta,
    ): Subsumsjon =
        GodkjenningsSubsumsjon(
            "$navn [$indeks]",
            action,
            child.deepCopy(indeks, fakta),
            godkjenningsfakta.deepCopy(indeks, fakta) as List<GrunnleggendeFaktum<Boolean>>,
            oppfyltSubsumsjon.deepCopy(indeks, fakta),
            ikkeOppfyltSubsumsjon.deepCopy(indeks, fakta),
        )

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
