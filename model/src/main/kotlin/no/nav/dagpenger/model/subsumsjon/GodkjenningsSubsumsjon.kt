package no.nav.dagpenger.model.subsumsjon

import no.nav.dagpenger.model.faktum.Faktum
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.model.visitor.SubsumsjonVisitor

class GodkjenningsSubsumsjon private constructor(
    navn: String,
    private val action: Action,
    private val child: Subsumsjon,
    private val godkjenning: Faktum<Boolean>,
    gyldigSubsumsjon: Subsumsjon,
    ugyldigSubsumsjon: Subsumsjon
) : SammensattSubsumsjon(navn, mutableListOf(child), gyldigSubsumsjon, ugyldigSubsumsjon) {

    internal constructor(action: Action, child: Subsumsjon, godkjenning: Faktum<Boolean>) :
        this("${child.navn} godkjenning", action, child, godkjenning, TomSubsumsjon, TomSubsumsjon)

    enum class Action(internal val strategy: (Boolean, Boolean?) -> Boolean?) {
        JaAction({ childResultat: Boolean, godkjenningResultat: Boolean? -> childResultat && godkjenningResultat != false }),
        NeiAction({ childResultat: Boolean, godkjenningResultat: Boolean? -> childResultat || godkjenningResultat == false }),
        UansettAction({ childResultat: Boolean, godkjenningResultat: Boolean? ->
            if (godkjenningResultat != null)
                if (godkjenningResultat) childResultat else !childResultat
            else childResultat
        })
    }

    override fun lokaltResultat(): Boolean? {
        return child.resultat()?.let { childResultat ->
            action.strategy(childResultat, if (godkjenning.erBesvart()) godkjenning.svar() else null)
        }
    }

    override fun deepCopy(søknadprosess: Søknadprosess) = GodkjenningsSubsumsjon(
        navn,
        action,
        child.deepCopy(søknadprosess),
        søknadprosess.ja(godkjenning.id),
        gyldigSubsumsjon.deepCopy(søknadprosess),
        ugyldigSubsumsjon.deepCopy(søknadprosess)
    )

    override fun bygg(søknad: Søknad) = GodkjenningsSubsumsjon(
        navn,
        action,
        child.bygg(søknad),
        søknad.ja(godkjenning.faktumId),
        gyldigSubsumsjon.bygg(søknad),
        ugyldigSubsumsjon.bygg(søknad)
    )

    override fun deepCopy(): Subsumsjon {
        return GodkjenningsSubsumsjon(
            navn,
            action,
            child.deepCopy(),
            godkjenning,
            gyldigSubsumsjon.deepCopy(),
            ugyldigSubsumsjon.deepCopy()
        )
    }

    override fun deepCopy(indeks: Int, søknad: Søknad): Subsumsjon {
        return GodkjenningsSubsumsjon(
            "$navn [$indeks]",
            action,
            child.deepCopy(indeks, søknad),
            godkjenning,
            gyldigSubsumsjon.deepCopy(indeks, søknad),
            ugyldigSubsumsjon.deepCopy(indeks, søknad)
        )
    }

    override fun accept(visitor: SubsumsjonVisitor) {
        lokaltResultat().also {
            visitor.preVisit(this, it)
            super.accept(visitor)
            visitor.preVisit(this, action, it)
            godkjenning.accept(visitor)
            visitor.postVisit(this, action, it)
            visitor.postVisit(this, it)
        }
    }
}
