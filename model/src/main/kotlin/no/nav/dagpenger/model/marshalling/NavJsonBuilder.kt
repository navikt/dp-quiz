package no.nav.dagpenger.model.marshalling

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import no.nav.dagpenger.model.faktum.Dokument
import no.nav.dagpenger.model.faktum.Faktum
import no.nav.dagpenger.model.faktum.FaktumId
import no.nav.dagpenger.model.faktum.GeneratorFaktum
import no.nav.dagpenger.model.faktum.GrunnleggendeFaktum
import no.nav.dagpenger.model.faktum.GyldigeValg
import no.nav.dagpenger.model.faktum.Identer.Ident.Type
import no.nav.dagpenger.model.faktum.Inntekt
import no.nav.dagpenger.model.faktum.LandGrupper
import no.nav.dagpenger.model.faktum.Prosessversjon
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.seksjon.Seksjon
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.model.seksjon.Versjon
import no.nav.dagpenger.model.visitor.SøknadprosessVisitor
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

class NavJsonBuilder(søknadprosess: Søknadprosess, private val seksjonNavn: String, indeks: Int = 0) : SøknadprosessVisitor {
    private val mapper = ObjectMapper()
    private val root: ObjectNode = mapper.createObjectNode()
    private val faktaNode = mapper.createArrayNode()
    private val faktumIder = mutableSetOf<String>()
    private val behovNode = mapper.createArrayNode()
    private val identerNode = mapper.createArrayNode()
    private lateinit var faktumNavBehov: FaktumNavBehov
    private var ignore = true
    private var rootId = 0

    init {
        søknadprosess.søknad.accept(this)
        søknadprosess.first { seksjonNavn == it.navn && indeks == it.indeks }.filtrertSeksjon(søknadprosess.rootSubsumsjon).accept(this)
    }
    fun resultat() = root

    override fun preVisit(søknad: Søknad, prosessVersjon: Prosessversjon, uuid: UUID) {
        root.put("@event_name", "faktum_svar")
        root.put("@opprettet", "${LocalDateTime.now()}")
        root.put("@id", "${UUID.randomUUID()}")
        root.put("@behovId", "${UUID.randomUUID()}")
        root.put("søknad_uuid", "$uuid")
        root.put("seksjon_navn", seksjonNavn)
        root.set<ArrayNode>("fakta", faktaNode)
        root.set<ArrayNode>("@behov", behovNode)
        root.set<ArrayNode>("identer", identerNode)

        faktumNavBehov = Versjon.id(prosessVersjon).faktumNavBehov ?: throw IllegalArgumentException("Finner ikke oversettelse til navbehov, versjon: $prosessVersjon")
    }

    override fun visit(type: Type, id: String, historisk: Boolean) {
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
        templates: List<Faktum<*>>,
        roller: Set<Rolle>,
        clazz: Class<R>
    ) {

        if (ignore) return
        if (id in faktumIder) return
        if (avhengerAvFakta.all { it.erBesvart() }) {
            behovNode.add(faktumNavBehov[rootId])
            val jsonTemplates = mapper.createArrayNode()
            templates.forEach { template ->
                jsonTemplates.addObject().also {
                    it.put("id", template.id)
                    it.put("navn", template.navn)
                    it.put("type", template.type().simpleName.lowercase())
                }
            }
            lagFaktumNode(id, "generator", jsonTemplates)
            avhengerAvFakta.forEach {
                root.putR(faktumNavBehov[it.reflection { rootId, _ -> rootId }], it.svar())
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
        landGrupper: LandGrupper?
    ) {
        if (ignore) return
        if (id in faktumIder) return
        if (avhengerAvFakta.all { it.erBesvart() }) {
            behovNode.add(faktumNavBehov[rootId])
            lagFaktumNode(id, clazz.simpleName.lowercase())
            avhengerAvFakta.forEach {
                root.putR(faktumNavBehov[it.reflection { rootId, _ -> rootId }], it.svar())
            }
        }
    }

    private fun lagFaktumNode(id: String, type: String, templates: ArrayNode? = null) {
        if (ignore) return
        if (id in faktumIder) return
        faktaNode.addObject().also { faktumNode ->
            faktumNode.put("id", id)
            faktumNode.put("behov", faktumNavBehov[rootId])
            faktumNode.put("type", type)
            if (templates != null) faktumNode.set<ArrayNode>("templates", templates)
        }
        faktumIder.add(id)
    }

    private fun ObjectNode.putR(key: String, svar: Any) {
        when (svar) {
            is Boolean -> this.put(key, svar)
            is Int -> this.put(key, svar)
            is Double -> this.put(key, svar)
            is String -> this.put(key, svar)
            is LocalDate -> this.put(key, svar.toString())
            is Dokument -> this.set(
                key,
                svar.reflection { lastOppTidsstempel, urn: String ->
                    mapper.createObjectNode().also {
                        it.put("lastOppTidsstempel", lastOppTidsstempel.toString())
                        it.put("urn", urn)
                    }
                }
            )
            is Inntekt -> this.put(key, svar.reflection { årlig, _, _, _ -> årlig })
            else -> throw IllegalArgumentException("Ukjent datatype")
        }
    }
}
