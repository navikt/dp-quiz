package no.nav.dagpenger.model.marshalling

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import no.nav.dagpenger.model.faktum.Faktum
import no.nav.dagpenger.model.faktum.FaktumId
import no.nav.dagpenger.model.faktum.GeneratorFaktum
import no.nav.dagpenger.model.faktum.GrunnleggendeFaktum
import no.nav.dagpenger.model.faktum.GyldigeValg
import no.nav.dagpenger.model.faktum.Prosessversjon
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.faktum.TemplateFaktum
import no.nav.dagpenger.model.marshalling.FaktumsvarTilJson.isBoolean
import no.nav.dagpenger.model.marshalling.FaktumsvarTilJson.lagBeskrivendeIderForGyldigeBoolskeValg
import no.nav.dagpenger.model.seksjon.Seksjon
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.model.visitor.FaktumVisitor
import no.nav.dagpenger.model.visitor.SøknadprosessVisitor
import java.util.UUID

class SøknadsmalJsonBuilder(søknadprosess: Søknadprosess) : SøknadprosessVisitor {

    companion object {
        private val mapper = ObjectMapper()
    }

    private val root: ObjectNode = mapper.createObjectNode()
    private lateinit var faktaNode: ArrayNode
    private val seksjoner = mapper.createArrayNode()
    private lateinit var gjeldendeSeksjon: ObjectNode
    private var rootId = 0
    private val faktumIder = mutableSetOf<String>()
    private var erISeksjon = false

    init {
        søknadprosess.søknad.accept(this)
        søknadprosess.forEach { it.accept(this) }
    }

    fun resultat() = root

    override fun preVisit(søknad: Søknad, prosessVersjon: Prosessversjon, uuid: UUID) {
        root.put("@event_name", "Søknadsmal")
        root.put("versjon_id", prosessVersjon.versjon)
        root.put("versjon_navn", prosessVersjon.prosessnavn.id)
    }

    override fun postVisit(søknad: Søknad, prosessVersjon: Prosessversjon, uuid: UUID) {
        root.set<ArrayNode>("seksjoner", seksjoner)
    }

    override fun preVisit(seksjon: Seksjon, rolle: Rolle, fakta: Set<Faktum<*>>, indeks: Int) {
        erISeksjon = true
        gjeldendeSeksjon = mapper.createObjectNode()
        faktaNode = mapper.createArrayNode()
        gjeldendeSeksjon.put("beskrivendeId", seksjon.navn)
    }

    override fun postVisit(seksjon: Seksjon, rolle: Rolle, indeks: Int) {
        gjeldendeSeksjon.set<ArrayNode>("fakta", faktaNode)
        seksjoner.add(gjeldendeSeksjon)
        erISeksjon = false
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
        if (!erISeksjon) return
        if (id in faktumIder) return
        addFaktum(faktum, id)
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
        gyldigeValg: GyldigeValg?
    ) {
        if (!erISeksjon) return
        if (id in faktumIder) return
        addFaktum(faktum, id)
    }

    private fun <R : Comparable<R>> addFaktum(faktum: Faktum<R>, id: String) {
        faktaNode.add(SøknadFaktumVisitor(faktum).root)
        faktumIder.add(id)
    }

    private class SøknadFaktumVisitor(
        faktum: Faktum<*>
    ) :
        FaktumVisitor {

        val root: ObjectNode = mapper.createObjectNode()

        init {
            faktum.accept(this)
        }

        override fun <R : Comparable<R>> visit(
            faktum: TemplateFaktum<R>,
            id: String,
            avhengigeFakta: Set<Faktum<*>>,
            avhengerAvFakta: Set<Faktum<*>>,
            roller: Set<Rolle>,
            clazz: Class<R>,
            gyldigeValg: GyldigeValg?
        ) {
            var overstyrbareGyldigeValg = gyldigeValg
            if (clazz.isBoolean()) {
                overstyrbareGyldigeValg = faktum.lagBeskrivendeIderForGyldigeBoolskeValg()
            }
            lagFaktumNode(id, clazz.simpleName.lowercase(), faktum.navn, roller, null, overstyrbareGyldigeValg)
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

            val jsonTemplates = mapper.createArrayNode()
            templates.forEach { template ->
                jsonTemplates.add(SøknadFaktumVisitor(template).root)
            }
            lagFaktumNode(id, "generator", faktum.navn, roller, jsonTemplates)
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
            gyldigeValg: GyldigeValg?
        ) {
            var overstyrbareGyldigeValg = gyldigeValg
            if (clazz.isBoolean()) {
                overstyrbareGyldigeValg = faktum.lagBeskrivendeIderForGyldigeBoolskeValg()
            }
            lagFaktumNode(id, clazz.simpleName.lowercase(), faktum.navn, roller, null, overstyrbareGyldigeValg)
        }

        private fun lagFaktumNode(
            id: String,
            clazz: String,
            navn: String? = null,
            roller: Set<Rolle>,
            templates: ArrayNode? = null,
            gyldigeValg: GyldigeValg? = null
        ) {

            root.also { faktumNode ->
                faktumNode.put("id", id)
                faktumNode.put("type", clazz)
                faktumNode.put("beskrivendeId", navn)
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
                if (templates != null) faktumNode.set<ArrayNode>("templates", templates)
            }
        }
    }
}
