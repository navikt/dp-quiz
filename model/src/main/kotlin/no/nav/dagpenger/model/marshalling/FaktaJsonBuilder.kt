package no.nav.dagpenger.model.marshalling

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import no.nav.dagpenger.model.faktum.Faktum
import no.nav.dagpenger.model.faktum.FaktumId
import no.nav.dagpenger.model.faktum.GeneratorFaktum
import no.nav.dagpenger.model.faktum.GrunnleggendeFaktum
import no.nav.dagpenger.model.faktum.GyldigeValg
import no.nav.dagpenger.model.faktum.Identer
import no.nav.dagpenger.model.faktum.Prosessversjon
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.faktum.TemplateFaktum
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.model.visitor.SøknadprosessVisitor
import java.time.LocalDateTime
import java.util.UUID

class FaktaJsonBuilder(søknadprosess: Søknadprosess) : SøknadprosessVisitor {
    private val mapper = ObjectMapper()
    private val root: ObjectNode = mapper.createObjectNode()
    private val faktaNode = mapper.createArrayNode()
    private var rootId = 0
    private val faktumIder = mutableSetOf<String>()
    protected val identerNode = mapper.createArrayNode()

    init {
        søknadprosess.søknad.accept(this)
    }

    fun resultat() = root

    override fun preVisit(søknad: Søknad, prosessVersjon: Prosessversjon, uuid: UUID) {
        root.put("@event_name", "NySøknad")
        root.put("@opprettet", "${LocalDateTime.now()}")
        root.put("@id", "${UUID.randomUUID()}")
        root.put("søknad_uuid", "$uuid")
        root.put(
            "fødselsnummer",
            identerNode.first {
                it.get("type").asText().equals("folkeregisterident")
            }.get("id").asText()
        )
        root.set("fakta", faktaNode)
    }

    override fun visit(type: Identer.Ident.Type, id: String, historisk: Boolean) {
        identerNode.addObject().also { identNode ->
            identNode.put("id", id)
            identNode.put("type", type.name.lowercase())
            identNode.put("historisk", historisk)
        }
    }

    override fun visit(faktumId: FaktumId, rootId: Int, indeks: Int) {
        this.rootId = rootId
    }

    override fun <R : Comparable<R>> visit(
        faktum: TemplateFaktum<R>,
        id: String,
        avhengigeFakta: Set<Faktum<*>>,
        avhengerAvFakta: Set<Faktum<*>>,
        roller: Set<Rolle>,
        clazz: Class<R>
    ) {
        if (id in faktumIder) return
        lagFaktumNode(id, clazz.simpleName.lowercase(), faktum.navn, roller)
    }

    override fun <R : Comparable<R>> visit(
        faktum: GeneratorFaktum,
        id: String,
        avhengigeFakta: Set<Faktum<*>>,
        avhengerAvFakta: Set<Faktum<*>>,
        templates: List<Faktum<*>>,
        roller: Set<Rolle>,
        clazz: Class<R>
    ) {

        if (id in faktumIder) return

        val jsonTemplates = mapper.createArrayNode()
        templates.forEach { template ->
            jsonTemplates.addObject().also {
                it.put("id", template.id)
                it.put("navn", template.navn)
                it.put("clazz", template.clazz().simpleName.lowercase())
            }
        }
        lagFaktumNode(id, "generator", null, roller, jsonTemplates)
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
        gyldigeValg: GyldigeValg?
    ) {
        if (id in faktumIder) return
        lagFaktumNode(id, clazz.simpleName.lowercase(), faktum.navn, roller, null, gyldigeValg)
    }

    private fun lagFaktumNode(
        id: String,
        clazz: String,
        navn: String? = null,
        roller: Set<Rolle>,
        templates: ArrayNode? = null,
        gyldigeValg: GyldigeValg? = null
    ) {
        if (id in faktumIder) return
        faktaNode.addObject().also { faktumNode ->
            faktumNode.put("id", id)
            faktumNode.put("clazz", clazz)
            faktumNode.put("navn", navn)
            faktumNode.putArray("roller").also { arrayNode ->
                roller.forEach { rolle ->
                    arrayNode.add(rolle.typeNavn)
                }
            }
            gyldigeValg?.let { gv ->
                faktumNode.putArray("gyldigeValg").also { arrayNode ->
                    gv.forEach {
                        arrayNode.add(it)
                    }
                }
            }
            if (templates != null) faktumNode["templates"] = templates
        }
        faktumIder.add(id)
    }
}
