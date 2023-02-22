package no.nav.dagpenger.model.marshalling

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import no.nav.dagpenger.model.faktum.Fakta
import no.nav.dagpenger.model.faktum.Faktaversjon
import no.nav.dagpenger.model.faktum.Faktum
import no.nav.dagpenger.model.faktum.GeneratorFaktum
import no.nav.dagpenger.model.faktum.GrunnleggendeFaktum
import no.nav.dagpenger.model.faktum.GyldigeValg
import no.nav.dagpenger.model.faktum.LandGrupper
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.TemplateFaktum
import no.nav.dagpenger.model.marshalling.FaktumTilJsonHjelper.erBoolean
import no.nav.dagpenger.model.marshalling.FaktumTilJsonHjelper.erLand
import no.nav.dagpenger.model.marshalling.FaktumTilJsonHjelper.lagBeskrivendeIderForGyldigeBoolskeValg
import no.nav.dagpenger.model.marshalling.FaktumTilJsonHjelper.lagFaktumNode
import no.nav.dagpenger.model.marshalling.FaktumTilJsonHjelper.leggTilGyldigeLand
import no.nav.dagpenger.model.marshalling.FaktumTilJsonHjelper.leggTilLandGrupper
import no.nav.dagpenger.model.seksjon.Prosess
import no.nav.dagpenger.model.seksjon.Seksjon
import no.nav.dagpenger.model.visitor.FaktumVisitor
import no.nav.dagpenger.model.visitor.ProsessVisitor
import java.util.UUID

class SøknadsmalJsonBuilder(prosess: Prosess) : ProsessVisitor {
    companion object {
        private val mapper = ObjectMapper()
    }

    private val root: ObjectNode = mapper.createObjectNode()
    private val seksjoner = mapper.createArrayNode()
    private lateinit var fakta: MutableSet<Faktum<*>>
    private val generatorFakta = mutableMapOf<GeneratorFaktum, List<Faktum<*>>>()

    init {
        prosess.accept(this)
    }

    fun resultat() = root

    override fun preVisit(fakta: Fakta, faktaversjon: Faktaversjon, uuid: UUID) {
        root.put("@event_name", "Søknadsmal")
        root.put("versjon_id", faktaversjon.versjon)
        root.put("versjon_navn", faktaversjon.faktatype.id)
    }

    override fun postVisit(fakta: Fakta, uuid: UUID) {
        root.set<ArrayNode>("seksjoner", seksjoner)
    }

    override fun preVisit(seksjon: Seksjon, rolle: Rolle, fakta: Set<Faktum<*>>, indeks: Int) {
        this.fakta = fakta.filter { it !is TemplateFaktum<*> }.toMutableSet()
    }

    override fun postVisit(seksjon: Seksjon, rolle: Rolle, indeks: Int) {
        if (rolle != Rolle.søker) return
        val faktaNode = fakta.fold(mapper.createArrayNode()) { acc, faktum ->
            acc.add(SøknadFaktumVisitor(faktum).root)
        }
        mapper.createObjectNode().apply {
            put("beskrivendeId", seksjon.navn)
            set<ArrayNode>("fakta", faktaNode)
            seksjoner.add(this)
        }
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
        generatorFakta.putIfAbsent(faktum, templates)
        if (!::fakta.isInitialized) return
        fakta.addAll(avhengerAvFakta)
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
        if (!::fakta.isInitialized) return
        fakta.addAll(avhengerAvFakta)
    }

    override fun <R : Comparable<R>> visit(
        faktum: TemplateFaktum<R>,
        id: String,
        avhengigeFakta: Set<Faktum<*>>,
        avhengerAvFakta: Set<Faktum<*>>,
        roller: Set<Rolle>,
        clazz: Class<R>,
        gyldigeValg: GyldigeValg?,
    ) {
        if (!::fakta.isInitialized) return
        fakta.addAll(
            generatorFakta.filter { (_, templates) ->
                templates.contains(faktum)
            }.keys,
        )
    }

    private class SøknadFaktumVisitor(
        faktum: Faktum<*>,
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
            gyldigeValg: GyldigeValg?,
        ) {
            var overstyrbareGyldigeValg = gyldigeValg

            if (clazz.erBoolean()) {
                overstyrbareGyldigeValg = faktum.lagBeskrivendeIderForGyldigeBoolskeValg()
            }
            this.root.lagFaktumNode<R>(
                id,
                clazz.simpleName.lowercase(),
                faktum.navn,
                roller,
                null,
                overstyrbareGyldigeValg,
            )
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
            val jsonTemplates = mapper.createArrayNode()
            templates.forEach { template ->
                jsonTemplates.add(SøknadFaktumVisitor(template).root)
            }
            this.root.lagFaktumNode<R>(id, "generator", faktum.navn, roller, jsonTemplates)
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
            var overstyrbareGyldigeValg = gyldigeValg
            if (clazz.erBoolean()) {
                overstyrbareGyldigeValg = faktum.lagBeskrivendeIderForGyldigeBoolskeValg()
            }
            this.root.lagFaktumNode<R>(
                id,
                clazz.simpleName.lowercase(),
                faktum.navn,
                roller,
                null,
                overstyrbareGyldigeValg,
            )
            if (clazz.erLand()) {
                this.root.leggTilGyldigeLand()
                this.root.leggTilLandGrupper(landGrupper)
            }
        }
    }
}
