package no.nav.dagpenger.model.subsumsjon

import no.nav.dagpenger.model.factory.FaktaRegel
import no.nav.dagpenger.model.faktum.Dokument
import no.nav.dagpenger.model.faktum.Faktum
import no.nav.dagpenger.model.faktum.GeneratorFaktum
import no.nav.dagpenger.model.faktum.GrunnleggendeFaktum
import no.nav.dagpenger.model.faktum.GyldigeValg
import no.nav.dagpenger.model.faktum.LandGrupper
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.faktum.TemplateFaktum
import no.nav.dagpenger.model.faktum.UtledetFaktum
import no.nav.dagpenger.model.regel.er
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.model.visitor.SubsumsjonVisitor

abstract class Subsumsjon protected constructor(
    internal val navn: String,
    oppfyltSubsumsjon: Subsumsjon?,
    ikkeOppfyltSubsumsjon: Subsumsjon?
) : Iterable<Subsumsjon> {
    protected lateinit var oppfyltSubsumsjon: Subsumsjon
    protected lateinit var ikkeOppfyltSubsumsjon: Subsumsjon

    init {
        if (oppfyltSubsumsjon != null) this.oppfyltSubsumsjon = oppfyltSubsumsjon
        if (ikkeOppfyltSubsumsjon != null) this.ikkeOppfyltSubsumsjon = ikkeOppfyltSubsumsjon
    }

    internal constructor(navn: String) : this(navn, TomSubsumsjon, TomSubsumsjon)

    open fun resultat(): Boolean? = when (lokaltResultat()) {
        true -> if (oppfylt is TomSubsumsjon) true else oppfylt.resultat()
        false -> if (ikkeOppfylt is TomSubsumsjon) false else ikkeOppfylt.resultat()
        null -> null
    }

    open fun saksbehandlerForklaring(): String = "saksbehandlerforklaring"

    abstract fun deepCopy(søknadprosess: Søknadprosess): Subsumsjon

    internal abstract fun bygg(søknad: Søknad): Subsumsjon

    abstract fun deepCopy(): Subsumsjon

    internal abstract fun deepCopy(indeks: Int, søknad: Søknad): Subsumsjon

    internal abstract fun lokaltResultat(): Boolean?

    internal abstract fun nesteFakta(): Set<GrunnleggendeFaktum<*>>

    internal open fun accept(visitor: SubsumsjonVisitor) {
        visitor.preVisitOppfylt(this, oppfyltSubsumsjon)
        oppfyltSubsumsjon.accept(visitor)
        visitor.postVisitOppfylt(this, oppfyltSubsumsjon)

        visitor.preVisitIkkeOppfylt(this, ikkeOppfyltSubsumsjon)
        ikkeOppfyltSubsumsjon.accept(visitor)
        visitor.postVisitIkkeOppfylt(this, ikkeOppfyltSubsumsjon)
    }

    internal abstract operator fun get(indeks: Int): Subsumsjon

    internal val oppfylt get() = oppfyltSubsumsjon

    internal fun oppfylt(child: Subsumsjon) {
        this.oppfyltSubsumsjon = child
    }

    internal val ikkeOppfylt get() = ikkeOppfyltSubsumsjon

    internal fun ikkeOppfylt(child: Subsumsjon) {
        this.ikkeOppfyltSubsumsjon = child
    }

    internal fun mulige(): Subsumsjon = this.deepCopy()._mulige()

    internal open fun _mulige(): Subsumsjon = this.also { copy ->
        when (lokaltResultat()) {
            true -> {
                copy.ikkeOppfylt(TomSubsumsjon)
                copy.oppfyltSubsumsjon._mulige()
            }
            false -> {
                copy.oppfylt(TomSubsumsjon)
                copy.ikkeOppfyltSubsumsjon._mulige()
            }
            null -> {
                copy.oppfyltSubsumsjon._mulige()
                copy.ikkeOppfyltSubsumsjon._mulige()
            }
        }
    }

    internal fun relevanteFakta() = RelevanteFakta(this).resultater
    internal abstract fun alleFakta(): List<Faktum<*>>

    private class RelevanteFakta(subsumsjon: Subsumsjon) : SubsumsjonVisitor {
        val resultater = mutableSetOf<Faktum<*>>()
        private var ignore = false

        init {
            subsumsjon.mulige().accept(this)
        }

        override fun preVisit(
            subsumsjon: GodkjenningsSubsumsjon,
            action: GodkjenningsSubsumsjon.Action,
            lokaltResultat: Boolean?
        ) {
            ignore = when (action) {
                GodkjenningsSubsumsjon.Action.JaAction -> lokaltResultat == false
                GodkjenningsSubsumsjon.Action.NeiAction -> lokaltResultat == true
                GodkjenningsSubsumsjon.Action.UansettAction -> false
            }
            if (lokaltResultat == null) ignore = true
        }

        override fun postVisit(
            subsumsjon: GodkjenningsSubsumsjon,
            action: GodkjenningsSubsumsjon.Action,
            lokaltResultat: Boolean?
        ) {
            ignore = false
        }

        override fun <R : Comparable<R>> visitUtenSvar(
            faktum: GrunnleggendeFaktum<R>,
            tilstand: Faktum.FaktumTilstand,
            id: String,
            avhengigeFakta: Set<Faktum<*>>,
            avhengerAvFakta: Set<Faktum<*>>,
            godkjenner: Set<Faktum<*>>,
            roller: Set<Rolle>,
            clazz: Class<R>,
            gyldigeValg: GyldigeValg?,
            landGrupper: LandGrupper?
        ) {
            if (!ignore) {
                resultater.add(faktum)
            }
        }

        override fun <R : Comparable<R>> visitMedSvar(
            faktum: GrunnleggendeFaktum<R>,
            tilstand: Faktum.FaktumTilstand,
            id: String,
            avhengigeFakta: Set<Faktum<*>>,
            avhengerAvFakta: Set<Faktum<*>>,
            godkjenner: Set<Faktum<*>>,
            roller: Set<Rolle>,
            clazz: Class<R>,
            svar: R,
            besvartAv: String?,
            gyldigeValg: GyldigeValg?,
            landGrupper: LandGrupper?
        ) {
            if (!ignore) {
                resultater.add(faktum)
            }
        }

        override fun <R : Comparable<R>> visitUtenSvar(
            faktum: GeneratorFaktum,
            id: String,
            avhengigeFakta: Set<Faktum<*>>,
            avhengerAvFakta: Set<Faktum<*>>,
            templates: List<TemplateFaktum<*>>,
            roller: Set<Rolle>,
            clazz: Class<R>
        ) {
            if (!ignore) {
                resultater.add(faktum)
            }
        }

        override fun <R : Comparable<R>> visitMedSvar(
            faktum: GeneratorFaktum,
            id: String,
            avhengigeFakta: Set<Faktum<*>>,
            avhengerAvFakta: Set<Faktum<*>>,
            templates: List<TemplateFaktum<*>>,
            roller: Set<Rolle>,
            clazz: Class<R>,
            svar: R,
            genererteFaktum: Set<Faktum<*>>
        ) {
            if (!ignore) {
                resultater.add(faktum)
            }
        }

        override fun <R : Comparable<R>> preVisit(
            faktum: UtledetFaktum<R>,
            id: String,
            avhengigeFakta: Set<Faktum<*>>,
            avhengerAvFakta: Set<Faktum<*>>,
            children: Set<Faktum<*>>,
            clazz: Class<R>,
            regel: FaktaRegel<R>
        ) {
            if (!ignore) {
                resultater.add(faktum)
            }
        }

        override fun <R : Comparable<R>> preVisit(
            faktum: UtledetFaktum<R>,
            id: String,
            avhengigeFakta: Set<Faktum<*>>,
            avhengerAvFakta: Set<Faktum<*>>,
            children: Set<Faktum<*>>,
            clazz: Class<R>,
            regel: FaktaRegel<R>,
            svar: R
        ) {
            if (!ignore) {
                resultater.add(faktum)
            }
        }
    }
}

fun String.alle(vararg subsumsjoner: Subsumsjon): Subsumsjon {
    return AlleSubsumsjon(this, subsumsjoner.toList())
}

fun String.minstEnAv(vararg subsumsjoner: Subsumsjon): Subsumsjon {
    return MinstEnAvSubsumsjon(this, subsumsjoner.toList())
}

fun String.bareEnAv(vararg subsumsjoner: Subsumsjon): Subsumsjon = BareEnAvSubsumsjon(this, subsumsjoner.toList())

infix fun Subsumsjon.hvisOppfylt(block: SubsumsjonGenerator) = this.also {
    require(it.oppfylt is TomSubsumsjon) { " Kan ikke overskrive oppfylt gren, er allerede satt til subsumsjon '${it.oppfylt.navn}'" }
    this.oppfylt(block())
}

infix fun Subsumsjon.hvisIkkeOppfylt(block: SubsumsjonGenerator) = this.also {
    require(it.ikkeOppfylt is TomSubsumsjon) { " Kan ikke overskrive IKKE oppfylt gren, er allerede satt til subsumsjon '${it.ikkeOppfylt.navn}'" }
    this.ikkeOppfylt(block())
}

infix fun Subsumsjon.hvisIkkeOppfyltManuell(manuellFaktum: Faktum<Boolean>) = hvisIkkeOppfylt { manuellFaktum er true }
infix fun Subsumsjon.hvisOppfyltManuell(manuellFaktum: Faktum<Boolean>) = hvisOppfylt { manuellFaktum er true }

infix fun String.deltre(block: SubsumsjonGenerator) = DeltreSubsumsjon(this, block())

infix fun Subsumsjon.uansett(block: SubsumsjonGenerator): Subsumsjon {
    val child = block()
    return this.also {
        this.oppfylt(child)
        this.ikkeOppfylt(child)
    }
}

infix fun Subsumsjon.sannsynliggjøresAv(dokumentFaktum: Faktum<Dokument>) = SannsynliggjøringsSubsumsjon(
    this,
    dokumentFaktum
)
typealias SubsumsjonGenerator = () -> Subsumsjon
