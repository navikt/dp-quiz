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
import no.nav.dagpenger.model.faktum.Land
import no.nav.dagpenger.model.faktum.Periode
import no.nav.dagpenger.model.faktum.Prosessversjon
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.faktum.Tekst
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
        addFaktum(faktum, id, genererte)
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
        if (faktum.faktumId.harIndeks()) {
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
        if (faktum.faktumId.harIndeks()) {
            erGenerertFraTemplate.add(faktum)
            return
        }
        addFaktum(faktum, id)
    }

    private fun <R : Comparable<R>> addFaktum(faktum: Faktum<R>, id: String) {
        faktaNode.add(SøknadFaktumVisitor(faktum, emptyList()).root)
        faktumIder.add(id)
    }

    private fun <R : Comparable<R>> addFaktum(faktum: Faktum<R>, id: String, generatorFaktum: List<Faktum<*>>) {
        faktaNode.add(SøknadFaktumVisitor(faktum, generatorFaktum).root)
        faktumIder.add(id)
    }

    private class SøknadFaktumVisitor(
        faktum: Faktum<*>,
        private val genererteFaktum: List<Faktum<*>> = emptyList()
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
            lagFaktumNode(id, "generator", faktum.navn, roller, jsonTemplates, svar = svar)
            val svarListe = mapper.createArrayNode()

            (1..faktum.svar()).forEach { i ->
                val indeks = mapper.createArrayNode()
                genererteFaktum.filter { faktum ->
                    faktum.faktumId.reflection { _, indeks ->
                        indeks == i
                    }
                }.filter { it.erBesvart() }.forEach {
                    indeks.add(SøknadFaktumVisitor(it).root)
                }
                svarListe.add(indeks)
            }
            this.root["svar"] = svarListe
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
                svar?.also { faktumNode.putR("svar", it) }
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

        private fun <R> ObjectNode.putR(beskrivendeId: String = "svar", svar: R) {
            when (svar) {
                is Boolean -> this.put(beskrivendeId, svar)
                is Int -> this.put(beskrivendeId, svar)
                is Double -> this.put(beskrivendeId, svar)
                is String -> this.put(beskrivendeId, svar)
                is LocalDate -> this.put(beskrivendeId, svar.toString())
                is Tekst -> this.put(beskrivendeId, svar.verdi)
                is Dokument -> this.set(beskrivendeId, svar.asJsonNode())
                is Periode -> this.set(beskrivendeId, svar.asJsonNode())
                is Flervalg -> this.set(beskrivendeId, svar.asJsonNode())
                is Envalg -> this.put(beskrivendeId, svar.first())
                is Land -> this.put(beskrivendeId, svar.alpha3Code)
                is Inntekt -> this.put(beskrivendeId, svar.asJsonNode())
                else -> throw IllegalArgumentException("Ukjent datatype ${svar!!::class.simpleName}")
            }
        }

        private fun Dokument.asJsonNode() =
            reflection { lastOppTidsstempel, urn: String ->
                mapper.createObjectNode().also {
                    it.put("lastOppTidsstempel", lastOppTidsstempel.toString())
                    it.put("urn", urn)
                }
            }

        private fun Periode.asJsonNode() =
            reflection { fom, tom ->
                mapper.createObjectNode().also {
                    it.put("fom", fom.toString())
                    it.put("tom", tom?.toString())
                }
            }

        private fun Flervalg.asJsonNode(): ArrayNode? {
            val flervalg = mapper.createArrayNode()
            forEach { flervalg.add(it) }
            return flervalg
        }

        private fun Inntekt.asJsonNode() = reflection { årlig, _, _, _ -> årlig }
    }
}
