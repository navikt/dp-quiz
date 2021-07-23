package no.nav.dagpenger.model.marshalling

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import no.nav.dagpenger.model.factory.FaktaRegel
import no.nav.dagpenger.model.faktum.Dokument
import no.nav.dagpenger.model.faktum.Faktum
import no.nav.dagpenger.model.faktum.GeneratorFaktum
import no.nav.dagpenger.model.faktum.GrunnleggendeFaktum
import no.nav.dagpenger.model.faktum.Identer
import no.nav.dagpenger.model.faktum.Inntekt
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.faktum.UtledetFaktum
import no.nav.dagpenger.model.marshalling.Språk.Companion.bokmål
import no.nav.dagpenger.model.regel.Regel
import no.nav.dagpenger.model.seksjon.Seksjon
import no.nav.dagpenger.model.subsumsjon.AlleSubsumsjon
import no.nav.dagpenger.model.subsumsjon.BareEnAvSubsumsjon
import no.nav.dagpenger.model.subsumsjon.DeltreSubsumsjon
import no.nav.dagpenger.model.subsumsjon.EnkelSubsumsjon
import no.nav.dagpenger.model.subsumsjon.GodkjenningsSubsumsjon
import no.nav.dagpenger.model.subsumsjon.GodkjenningsSubsumsjon.Action
import no.nav.dagpenger.model.subsumsjon.MinstEnAvSubsumsjon
import no.nav.dagpenger.model.subsumsjon.Subsumsjon
import no.nav.dagpenger.model.visitor.SøknadprosessVisitor
import java.time.LocalDate
import java.util.Locale
import java.util.UUID

abstract class SøknadJsonBuilder(private val lokal: Locale = bokmål) : SøknadprosessVisitor {
    private val mapper = ObjectMapper()
    protected val root: ObjectNode = mapper.createObjectNode()
    protected val faktaNode = mapper.createArrayNode()
    protected val identerNode = mapper.createArrayNode()
    protected val subsumsjonRoot = mapper.createArrayNode()
    internal var ignore = true
    protected var ignoreSubsumsjon: Subsumsjon? = null
    private val faktumIder = mutableSetOf<String>()
    private val subsumsjonNoder = mutableListOf<ArrayNode>(subsumsjonRoot)
    open fun resultat() = root
    private var versjonId = 0
    private lateinit var språk: Språk

    override fun preVisit(søknad: Søknad, versjonId: Int, uuid: UUID) {
        this.versjonId = versjonId
        this.språk = Språk(lokal, versjonId)
    }

    override fun visit(type: Identer.Ident.Type, id: String, historisk: Boolean) {
        identerNode.addObject().also { identNode ->
            identNode.put("id", id)
            identNode.put("type", type.name.lowercase())
            identNode.put("historisk", historisk)
        }
    }

    override fun preVisit(seksjon: Seksjon, rolle: Rolle, fakta: Set<Faktum<*>>, indeks: Int) {
        ignore = false
    }

    override fun postVisit(seksjon: Seksjon, rolle: Rolle, indeks: Int) {
        ignore = true
    }

    override fun <R : Comparable<R>> visit(
        faktum: GrunnleggendeFaktum<R>,
        tilstand: Faktum.FaktumTilstand,
        id: String,
        avhengigeFakta: Set<Faktum<*>>,
        avhengerAvFakta: Set<Faktum<*>>,
        godkjenner: Set<Faktum<*>>,
        roller: Set<Rolle>,
        clazz: Class<R>
    ) {
        lagFaktumNode<R>(id, språk.oversett(faktum), roller, godkjenner, clazz, besvartAv = null)
    }

    override fun <R : Comparable<R>> visit(
        faktum: GrunnleggendeFaktum<R>,
        tilstand: Faktum.FaktumTilstand,
        id: String,
        avhengigeFakta: Set<Faktum<*>>,
        avhengerAvFakta: Set<Faktum<*>>,
        godkjenner: Set<Faktum<*>>,
        roller: Set<Rolle>,
        clazz: Class<R>,
        svar: R,
        besvartAv: String?
    ) {
        lagFaktumNode(id, faktum.navn, roller, godkjenner, clazz, svar, besvartAv)
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
        lagFaktumNode(id, faktum.navn, clazz = clazz)
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
        lagFaktumNode(id, faktum.navn, clazz = clazz, svar = svar)
    }

    override fun <R : Comparable<R>> visit(
        faktum: GeneratorFaktum,
        id: String,
        avhengigeFakta: Set<Faktum<*>>,
        avhengerAvFakta: Set<Faktum<*>>,
        templates: List<Faktum<*>>,
        roller: Set<Rolle>,
        clazz: Class<R>,
        svar: R
    ) {
        lagFaktumNode(id, faktum.navn, clazz = clazz, svar = svar)
    }

    override fun preVisit(
        subsumsjon: EnkelSubsumsjon,
        regel: Regel,
        fakta: List<Faktum<*>>,
        lokaltResultat: Boolean?,
        resultat: Boolean?
    ) {
        if (ignoreSubsumsjon != null) return
        subsumsjonNoder.first().addObject().also { subsumsjonNode ->
            subsumsjonNode.put("lokalt_resultat", lokaltResultat)
            subsumsjonNode.put("navn", subsumsjon.navn)
            subsumsjonNode.put("forklaring", subsumsjon.saksbehandlerForklaring())
            subsumsjonNode.put("type", "Enkel subsumsjon")
        }
    }

    override fun preVisit(subsumsjon: AlleSubsumsjon, lokaltResultat: Boolean?, resultat: Boolean?) {
        putSubsumsjon(lokaltResultat, subsumsjon, "Alle subsumsjon")
    }

    override fun postVisit(subsumsjon: AlleSubsumsjon, lokaltResultat: Boolean?, resultat: Boolean?) {
        if (ignoreSubsumsjon != null) return
        subsumsjonNoder.removeAt(0)
    }

    override fun preVisit(subsumsjon: MinstEnAvSubsumsjon, lokaltResultat: Boolean?, resultat: Boolean?) {
        putSubsumsjon(lokaltResultat, subsumsjon, "Minst en av subsumsjon")
    }

    override fun postVisit(subsumsjon: MinstEnAvSubsumsjon, lokaltResultat: Boolean?, resultat: Boolean?) {
        if (ignoreSubsumsjon != null) return
        subsumsjonNoder.removeAt(0)
    }

    override fun preVisit(subsumsjon: BareEnAvSubsumsjon, lokaltResultat: Boolean?, resultat: Boolean?) {
        putSubsumsjon(lokaltResultat, subsumsjon, "Bare en av subsumsjon")
    }

    override fun postVisit(subsumsjon: BareEnAvSubsumsjon, lokaltResultat: Boolean?, resultat: Boolean?) {
        if (ignoreSubsumsjon != null) return
        subsumsjonNoder.removeAt(0)
    }

    override fun preVisit(subsumsjon: DeltreSubsumsjon, lokaltResultat: Boolean?, resultat: Boolean?) {
        putSubsumsjon(lokaltResultat, subsumsjon, "Deltre subsumsjon")
    }

    override fun postVisit(subsumsjon: DeltreSubsumsjon, lokaltResultat: Boolean?, resultat: Boolean?) {
        if (ignoreSubsumsjon != null) return
        subsumsjonNoder.removeAt(0)
    }

    override fun preVisit(
        subsumsjon: GodkjenningsSubsumsjon,
        action: Action,
        godkjenning: List<GrunnleggendeFaktum<Boolean>>,
        lokaltResultat: Boolean?,
        childResultat: Boolean?
    ) {
        putSubsumsjon(lokaltResultat, subsumsjon, "Godkjenning subsumsjon")
    }

    override fun postVisit(
        subsumsjon: GodkjenningsSubsumsjon,
        action: Action,
        godkjenning: List<GrunnleggendeFaktum<Boolean>>,
        lokaltResultat: Boolean?,
        childResultat: Boolean?
    ) {
        if (ignoreSubsumsjon != null) return
        subsumsjonNoder.removeAt(0).also {
            if (godkjenning.all { it.erBesvart() } && when (action) {
                Action.JaAction -> childResultat == true
                Action.NeiAction -> childResultat == false
                Action.UansettAction -> true
            }
            )
                it.addObject().also { subsumsjonNode ->
                    subsumsjonNode.put("lokalt_resultat", godkjenning.all { it.svar() }) // TODO: Bytt ut med subsumsjon
                    subsumsjonNode.put("navn", "Godkjent med")
                    subsumsjonNode.put("forklaring", if (godkjenning.all { it.svar() }) "godkjent" else "ikke godkjent")
                    subsumsjonNode.put("type", "Godkjenningsubsumsjon")
                }
        }
    }

    override fun preVisitGyldig(parent: Subsumsjon, child: Subsumsjon) {
        if (ignoreSubsumsjon != null) return
        if (parent.lokaltResultat() == false) ignoreSubsumsjon = parent
    }

    override fun postVisitGyldig(parent: Subsumsjon, child: Subsumsjon) {
        if (parent == ignoreSubsumsjon) ignoreSubsumsjon = null
    }

    override fun preVisitUgyldig(parent: Subsumsjon, child: Subsumsjon) {
        if (ignoreSubsumsjon != null) return
        if (parent.lokaltResultat() == true) ignoreSubsumsjon = parent
    }

    override fun postVisitUgyldig(parent: Subsumsjon, child: Subsumsjon) {
        if (parent == ignoreSubsumsjon) ignoreSubsumsjon = null
    }

    protected open fun putSubsumsjon(
        lokaltResultat: Boolean?,
        subsumsjon: Subsumsjon,
        type: String
    ) {
        if (ignoreSubsumsjon != null) return
        subsumsjonNoder.first().addObject().also { subsumsjonNode ->
            subsumsjonNode.put("lokalt_resultat", lokaltResultat)
            subsumsjonNode.put("navn", subsumsjon.navn)
            subsumsjonNode.put("type", type)
            subsumsjonNode.put("forklaring", subsumsjon.saksbehandlerForklaring())
            subsumsjonNode.set("subsumsjoner", mapper.createArrayNode().also { subsumsjonNoder.add(0, it) })
        }
    }

    protected open fun <R : Comparable<R>> lagFaktumNode(
        id: String,
        navn: String,
        roller: Set<Rolle> = emptySet(),
        godkjenner: Set<Faktum<*>> = emptySet(),
        clazz: Class<R>,
        svar: R? = null,
        besvartAv: String? = null
    ) {
        if (ignore) return
        if (id in faktumIder) return
        faktaNode.addObject().also { faktumNode ->
            faktumNode.put("navn", navn)
            faktumNode.put("id", id)
            faktumNode.set("roller", mapper.valueToTree(roller.map { it.typeNavn }))
            faktumNode.put("type", clazz.simpleName.lowercase())
            faktumNode.set("godkjenner", mapper.valueToTree(godkjenner.map { it.id }))
            svar?.also { faktumNode.putR(it) }
            besvartAv?.also { faktumNode.put("besvartAv", it) }
        }
        faktumIder.add(id)
    }

    private fun <R : Comparable<R>> ObjectNode.putR(svar: R) {
        when (svar) {
            is Boolean -> this.put("svar", svar)
            is Int -> this.put("svar", svar)
            is Double -> this.put("svar", svar)
            is String -> this.put("svar", svar)
            is LocalDate -> this.put("svar", svar.toString())
            is Dokument -> this.set(
                "svar",
                svar.reflection { lastOppTidsstempel, url ->
                    mapper.createObjectNode().also {
                        it.put("lastOppTidsstempel", lastOppTidsstempel.toString())
                        it.put("url", url)
                    }
                }
            )
            is Inntekt -> this.put("svar", svar.reflection { årlig, _, _, _ -> årlig })
            else -> throw IllegalArgumentException("Ukjent datatype")
        }
    }
}
