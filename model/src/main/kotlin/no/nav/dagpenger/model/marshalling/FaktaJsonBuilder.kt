package no.nav.dagpenger.model.marshalling

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import no.nav.dagpenger.model.faktum.Dokument
import no.nav.dagpenger.model.faktum.Envalg
import no.nav.dagpenger.model.faktum.Faktum
import no.nav.dagpenger.model.faktum.FaktumId
import no.nav.dagpenger.model.faktum.Flervalg
import no.nav.dagpenger.model.faktum.GeneratorFaktum
import no.nav.dagpenger.model.faktum.GrunnleggendeFaktum
import no.nav.dagpenger.model.faktum.GyldigeValg
import no.nav.dagpenger.model.faktum.Identer
import no.nav.dagpenger.model.faktum.Inntekt
import no.nav.dagpenger.model.faktum.Prosessversjon
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.faktum.TemplateFaktum
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.model.visitor.FaktumVisitor
import no.nav.dagpenger.model.visitor.SøknadprosessVisitor
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

class FaktaJsonBuilder(søknadprosess: Søknadprosess) : SøknadprosessVisitor {

    companion object {
        private val mapper = ObjectMapper()
    }

    private val root: ObjectNode = mapper.createObjectNode()
    private val faktaNode = mapper.createArrayNode()
    private var rootId = 0
    private val faktumIder = mutableSetOf<String>()
    private val identerNode = mapper.createArrayNode()
    private val faktumTemplates = mutableMapOf<Faktum<*>, MutableList<Faktum<*>>>()
    private val erGenerertFraTemplate = mutableListOf<Faktum<*>>()

    init {
        søknadprosess.søknad.accept(this)
        søknadprosess.forEach { it.accept(this) }
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
        faktum: GeneratorFaktum,
        id: String,
        avhengigeFakta: Set<Faktum<*>>,
        avhengerAvFakta: Set<Faktum<*>>,
        templates: List<Faktum<*>>,
        roller: Set<Rolle>,
        clazz: Class<R>
    ) {

        if (id in faktumIder) return
        addFaktum(faktum, id)
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
        if (id in faktumIder) return
        val genererte = erGenerertFraTemplate.filter { generertFaktum ->
            templates.any { generertFaktum.faktumId.generertFra(it.faktumId) }
        }

        addFaktum(faktum, id)

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
        if(faktum.faktumId.harIndeks()) {
            erGenerertFraTemplate.add(faktum)
            return
        }
        addFaktum(faktum, id)
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
        besvartAv: String?,
        gyldigeValg: GyldigeValg?
    ) {
        if (id in faktumIder) return
        if(faktum.faktumId.harIndeks()) {
            erGenerertFraTemplate.add(faktum)
            return
        }
        addFaktum(faktum, id)
    }

    private fun <R : Comparable<R>> addFaktum(faktum: Faktum<R>, id: String) {
        faktaNode.add(SøknadFaktumVisitor(faktum).root)
        faktumIder.add(id)
    }

    private class SøknadFaktumVisitor(faktum: Faktum<*>) : FaktumVisitor {

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
            clazz: Class<R>
        ) {
            lagFaktumNode<R>(id, clazz.simpleName.lowercase(), faktum.navn, roller)
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

            val jsonTemplates = mapper.createArrayNode()
            templates.forEach { template ->
                jsonTemplates.add(SøknadFaktumVisitor(template).root)
            }
            lagFaktumNode<R>(id, "generator", faktum.navn, roller, jsonTemplates)
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
            val jsonTemplates = mapper.createArrayNode()
            templates.forEach { template ->
                jsonTemplates.add(SøknadFaktumVisitor(template).root)
            }
            lagFaktumNode<R>(id, "generator", faktum.navn, roller, jsonTemplates, svar = svar)
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
            besvartAv: String?,
            gyldigeValg: GyldigeValg?
        ) {
            lagFaktumNode(id, clazz.simpleName.lowercase(), faktum.navn, roller, null, gyldigeValg, svar, besvartAv)
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
            lagFaktumNode<R>(id, clazz.simpleName.lowercase(), faktum.navn, roller, null, gyldigeValg)
        }

        private fun <R : Comparable<R>> lagFaktumNode(
            id: String,
            clazz: String,
            navn: String? = null,
            roller: Set<Rolle>,
            templates: ArrayNode? = null,
            gyldigeValg: GyldigeValg? = null,
            svar: R? = null,
            besvartAv: String? = null
        ) {

            root.also { faktumNode ->
                faktumNode.put("id", id)
                faktumNode.put("type", clazz)
                faktumNode.put("beskrivendeId", navn)
                svar?.also { faktumNode.putR(it) }
                besvartAv?.also { faktumNode.put("besvartAv", it) }
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
                is Flervalg -> {
                    val flervalg = mapper.createArrayNode()
                    svar.forEach { flervalg.add(it) }
                    this.set("svar", flervalg)
                }
                is Envalg -> {
                    this.put("svar", svar.first())
                }
                is Inntekt -> this.put("svar", svar.reflection { årlig, _, _, _ -> årlig })
                else -> throw IllegalArgumentException("Ukjent datatype ${svar::class.simpleName}")
            }
        }
    }
}
