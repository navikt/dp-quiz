package no.nav.dagpenger.model.subsumsjon

import no.nav.dagpenger.model.fakta.Fakta
import no.nav.dagpenger.model.fakta.Faktum
import no.nav.dagpenger.model.søknad.Søknad
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

    internal enum class Action(internal val strategy: (Boolean, Boolean?) -> Boolean?) {
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

    override fun deepCopy(søknad: Søknad) = GodkjenningsSubsumsjon(
        navn,
        action,
        child.deepCopy(søknad),
        søknad.ja(godkjenning.id),
        gyldigSubsumsjon.deepCopy(søknad),
        ugyldigSubsumsjon.deepCopy(søknad)
    )

    override fun bygg(fakta: Fakta) = GodkjenningsSubsumsjon(
        navn,
        action,
        child.bygg(fakta),
        fakta.ja(godkjenning.faktumId),
        gyldigSubsumsjon.bygg(fakta),
        ugyldigSubsumsjon.bygg(fakta)
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

    override fun deepCopy(indeks: Int, fakta: Fakta): Subsumsjon {
        return GodkjenningsSubsumsjon(
            "$navn [$indeks]",
            action,
            child.deepCopy(indeks, fakta),
            godkjenning,
            gyldigSubsumsjon.deepCopy(indeks, fakta),
            ugyldigSubsumsjon.deepCopy(indeks, fakta)
        )
    }

    override fun accept(visitor: SubsumsjonVisitor) {
        resultat().also {
            visitor.preVisit(this, it)
            super.accept(visitor)
            godkjenning.accept(visitor)
            visitor.postVisit(this, it)
        }
    }
}
