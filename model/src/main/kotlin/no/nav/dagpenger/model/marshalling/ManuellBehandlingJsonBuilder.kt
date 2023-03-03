package no.nav.dagpenger.model.marshalling

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import no.nav.dagpenger.model.faktum.Fakta
import no.nav.dagpenger.model.faktum.Faktaversjon
import no.nav.dagpenger.model.faktum.Faktum
import no.nav.dagpenger.model.faktum.FaktumId
import no.nav.dagpenger.model.faktum.GeneratorFaktum
import no.nav.dagpenger.model.faktum.GrunnleggendeFaktum
import no.nav.dagpenger.model.faktum.GyldigeValg
import no.nav.dagpenger.model.faktum.Identer
import no.nav.dagpenger.model.faktum.LandGrupper
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.TemplateFaktum
import no.nav.dagpenger.model.marshalling.FaktumTilJsonHjelper.putR
import no.nav.dagpenger.model.seksjon.Prosess
import no.nav.dagpenger.model.seksjon.Seksjon
import no.nav.dagpenger.model.visitor.ProsessVisitor
import java.time.LocalDateTime
import java.util.UUID

class ManuellBehandlingJsonBuilder(prosess: Prosess, private val seksjonNavn: String, indeks: Int = 0) :
    ProsessVisitor {

    private val mapper = ObjectMapper()
    private val root: ObjectNode = mapper.createObjectNode()
    private val faktaNode = mapper.createArrayNode()
    private val faktumIder = mutableSetOf<String>()
    private val identerNode = mapper.createArrayNode()
    private var ignore = true
    private var rootId = 0

    init {
        prosess.fakta.accept(this)
        prosess.first { seksjonNavn == it.navn && indeks == it.indeks }.filtrertSeksjon(prosess.rootSubsumsjon).accept(this)
    }

    fun resultat() = root

    override fun preVisit(fakta: Fakta, faktaversjon: Faktaversjon, uuid: UUID, navBehov: FaktumNavBehov) {
        root.put("@event_name", "manuell_behandling")
        root.put("@opprettet", "${LocalDateTime.now()}")
        root.put("@id", "${UUID.randomUUID()}")
        root.put("søknad_uuid", "$uuid")
        root.put("seksjon_navn", seksjonNavn)
        root.set<ArrayNode>("fakta", faktaNode)
        root.set<ArrayNode>("identer", identerNode)
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

    override fun visit(faktumId: FaktumId, rootId: Int, indeks: Int) {
        this.rootId = rootId
    }

    override fun <R : Comparable<R>> visitUtenSvar(
        faktum: GeneratorFaktum,
        id: String,
        avhengigeFakta: Set<Faktum<*>>,
        avhengerAvFakta: Set<Faktum<*>>,
        templates: List<TemplateFaktum<*>>,
        roller: Set<Rolle>,
        clazz: Class<R>,
    ) {
        if (ignore) return
        if (id in faktumIder) return
        if (avhengerAvFakta.all { it.erBesvart() }) {
            val jsonTemplates = mapper.createArrayNode()
            templates.forEach { template ->
                jsonTemplates.addObject().also {
                    it.put("id", template.id)
                    it.put("navn", template.navn)
                    it.put("type", template.type().simpleName.lowercase())
                }
            }
            lagFaktumNode(id, faktum.navn)
            avhengerAvFakta.forEach {
                root.putR(it.reflection { rootId, _ -> rootId }, it.svar())
            }
        }
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
        landGrupper: LandGrupper?,
    ) {
        if (ignore) return
        if (id in faktumIder) return
        if (avhengerAvFakta.all { it.erBesvart() }) {
            lagFaktumNode(id, faktum.navn)
            avhengerAvFakta.forEach {
                root.putR(it.reflection { rootId, _ -> rootId }, it.svar())
            }
        }
    }

    private fun lagFaktumNode(id: String, navn: String) {
        if (ignore) return
        if (id in faktumIder) return
        faktaNode.addObject().also { faktumNode ->
            faktumNode.put("id", id)
            faktumNode.put("navn", navn)
        }
        faktumIder.add(id)
    }

    private fun ObjectNode.putR(key: Int, svar: Any) = putR(key.toString(), svar)
}
