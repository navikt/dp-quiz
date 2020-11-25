package no.nav.dagpenger.model.marshalling

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import no.nav.dagpenger.model.factory.FaktaRegel
import no.nav.dagpenger.model.faktum.Dokument
import no.nav.dagpenger.model.faktum.Faktum
import no.nav.dagpenger.model.faktum.GrunnleggendeFaktum
import no.nav.dagpenger.model.faktum.Identer
import no.nav.dagpenger.model.faktum.Inntekt
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.faktum.UtledetFaktum
import no.nav.dagpenger.model.faktum.ValgFaktum
import no.nav.dagpenger.model.regel.Regel
import no.nav.dagpenger.model.seksjon.Seksjon
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.model.subsumsjon.AlleSubsumsjon
import no.nav.dagpenger.model.subsumsjon.EnkelSubsumsjon
import no.nav.dagpenger.model.subsumsjon.GodkjenningsSubsumsjon
import no.nav.dagpenger.model.subsumsjon.GodkjenningsSubsumsjon.Action
import no.nav.dagpenger.model.subsumsjon.MakroSubsumsjon
import no.nav.dagpenger.model.subsumsjon.MinstEnAvSubsumsjon
import no.nav.dagpenger.model.subsumsjon.Subsumsjon
import no.nav.dagpenger.model.visitor.SøknadprosessVisitor
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

class SaksbehandlerJsonBuilder(
    søknadprosess: Søknadprosess,
    private val seksjonNavn: String,
    private val indeks: Int = 0
) : SøknadprosessVisitor {
    private val mapper = ObjectMapper()
    private val root: ObjectNode = mapper.createObjectNode()
    private val faktaNode = mapper.createArrayNode()
    private val identerNode = mapper.createArrayNode()
    private val subsumsjonRoot = mapper.createArrayNode()
    private var ignore = true
    private var iValg = false
    private val faktumIder = mutableSetOf<String>()
    private val subsumsjonNoder = mutableListOf<ArrayNode>(subsumsjonRoot)

    init {
        søknadprosess.søknad.accept(this)
        søknadprosess.first { seksjonNavn == it.navn && indeks == it.indeks }
            .filtrertSeksjon(søknadprosess.rootSubsumsjon).accept(this)
        søknadprosess.rootSubsumsjon.mulige().accept(this)
    }

    fun resultat() = root

    override fun preVisit(søknad: Søknad, versjonId: Int, uuid: UUID) {
        root.put("@event_name", "oppgave")
        root.put("@opprettet", "${LocalDateTime.now()}")
        root.put("@id", "${UUID.randomUUID()}")
        root.put("søknad_uuid", "$uuid")
        root.put("seksjon_navn", seksjonNavn)
        root.put("indeks", indeks)
        root.set("identer", identerNode)
        root.set("fakta", faktaNode)
        root.set("subsumsjoner", subsumsjonRoot)
    }

    override fun visit(type: Identer.Ident.Type, id: String, historisk: Boolean) {
        identerNode.addObject().also { identNode ->
            identNode.put("id", id)
            identNode.put("type", type.name.toLowerCase())
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
        if (iValg) return
        lagFaktumNode<R>(id, faktum.navn, roller, godkjenner)
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
        svar: R
    ) {
        lagFaktumNode(id, faktum.navn, roller, godkjenner, svar)
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
        lagFaktumNode<R>(id, faktum.navn)
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
        lagFaktumNode(id, faktum.navn, svar = svar)
    }

    override fun preVisit(
        faktum: ValgFaktum,
        id: String,
        avhengigeFakta: Set<Faktum<*>>,
        avhengerAvFakta: Set<Faktum<*>>,
        underordnedeJa: Set<Faktum<Boolean>>,
        underordnedeNei: Set<Faktum<Boolean>>,
        clazz: Class<Boolean>
    ) {
        lagFaktumNode<Boolean>(id, faktum.navn)
        iValg = true
    }

    override fun preVisit(
        faktum: ValgFaktum,
        id: String,
        avhengigeFakta: Set<Faktum<*>>,
        avhengerAvFakta: Set<Faktum<*>>,
        underordnedeJa: Set<Faktum<Boolean>>,
        underordnedeNei: Set<Faktum<Boolean>>,
        clazz: Class<Boolean>,
        svar: Boolean
    ) {
        lagFaktumNode(id, faktum.navn, svar = svar)
        iValg = true
    }

    override fun postVisit(
        faktum: ValgFaktum,
        id: String,
        underordnedeJa: Set<Faktum<Boolean>>,
        underordnedeNei: Set<Faktum<Boolean>>,
        clazz: Class<Boolean>
    ) {
        iValg = false
    }

    override fun preVisit(
        subsumsjon: EnkelSubsumsjon,
        regel: Regel,
        fakta: List<Faktum<*>>,
        lokaltResultat: Boolean?,
        resultat: Boolean?
    ) {
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
        subsumsjonNoder.removeAt(0)
    }

    override fun preVisit(subsumsjon: MinstEnAvSubsumsjon, lokaltResultat: Boolean?, resultat: Boolean?) {
        putSubsumsjon(lokaltResultat, subsumsjon, "Minst en av subsumsjon")
    }

    override fun postVisit(subsumsjon: MinstEnAvSubsumsjon, lokaltResultat: Boolean?, resultat: Boolean?) {
        subsumsjonNoder.removeAt(0)
    }

    override fun preVisit(subsumsjon: MakroSubsumsjon, lokaltResultat: Boolean?, resultat: Boolean?) {
        putSubsumsjon(lokaltResultat, subsumsjon, "Makro subsumsjon")
    }

    override fun postVisit(subsumsjon: MakroSubsumsjon, lokaltResultat: Boolean?, resultat: Boolean?) {
        subsumsjonNoder.removeAt(0)
    }

    override fun preVisit(
        subsumsjon: GodkjenningsSubsumsjon,
        action: Action,
        godkjenning: GrunnleggendeFaktum<Boolean>,
        lokaltResultat: Boolean?,
        childResultat: Boolean?
    ) {
        putSubsumsjon(lokaltResultat, subsumsjon, "Godkjenning subsumsjon")
    }

    override fun postVisit(
        subsumsjon: GodkjenningsSubsumsjon,
        action: Action,
        godkjenning: GrunnleggendeFaktum<Boolean>,
        lokaltResultat: Boolean?,
        childResultat: Boolean?
    ) {
        subsumsjonNoder.removeAt(0).also {
            if (godkjenning.erBesvart() && when (action) {
                Action.JaAction -> childResultat == true
                Action.NeiAction -> childResultat == false
                Action.UansettAction -> true
            }
            )
                it.addObject().also { subsumsjonNode ->
                    subsumsjonNode.put("lokalt_resultat", godkjenning.svar())
                    subsumsjonNode.put("navn", "Godkjent med")
                    subsumsjonNode.put("forklaring", if (godkjenning.svar()) "godkjent" else "ikke godkjent")
                    subsumsjonNode.put("type", "Godkjenningsubsumsjon")
                }
        }
    }

    private fun putSubsumsjon(
        lokaltResultat: Boolean?,
        subsumsjon: Subsumsjon,
        type: String
    ) = subsumsjonNoder.first().addObject().also { subsumsjonNode ->
        subsumsjonNode.put("lokalt_resultat", lokaltResultat)
        subsumsjonNode.put("navn", subsumsjon.navn)
        subsumsjonNode.put("type", type)
        subsumsjonNode.put("forklaring", subsumsjon.saksbehandlerForklaring())
        subsumsjonNode.set("subsumsjoner", mapper.createArrayNode().also { subsumsjonNoder.add(0, it) })
    }

    private fun <R : Comparable<R>> lagFaktumNode(
        id: String,
        navn: String,
        roller: Set<Rolle> = emptySet(),
        godkjenner: Set<Faktum<*>> = emptySet(),
        svar: R? = null
    ) {
        if (ignore) return
        if (id in faktumIder) return
        faktaNode.addObject().also { faktumNode ->
            faktumNode.put("navn", navn)
            faktumNode.put("id", id)
            faktumNode.set("roller", mapper.valueToTree(roller.map { it.typeNavn }))
            faktumNode.set("godkjenner", mapper.valueToTree(godkjenner.map { it.id }))
            svar?.also { faktumNode.putR(it) }
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
