package no.nav.dagpenger.model.marshalling

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import no.nav.dagpenger.model.faktum.Faktum
import no.nav.dagpenger.model.faktum.Faktum.Companion.erAlleBesvart
import no.nav.dagpenger.model.faktum.GeneratorFaktum
import no.nav.dagpenger.model.faktum.GrunnleggendeFaktum
import no.nav.dagpenger.model.faktum.GyldigeValg
import no.nav.dagpenger.model.faktum.Identer
import no.nav.dagpenger.model.faktum.LandGrupper
import no.nav.dagpenger.model.faktum.Prosessversjon
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.faktum.TemplateFaktum
import no.nav.dagpenger.model.marshalling.FaktumTilJsonHjelper.erBoolean
import no.nav.dagpenger.model.marshalling.FaktumTilJsonHjelper.erLand
import no.nav.dagpenger.model.marshalling.FaktumTilJsonHjelper.lagBeskrivendeIderForGyldigeBoolskeValg
import no.nav.dagpenger.model.marshalling.FaktumTilJsonHjelper.lagFaktumNode
import no.nav.dagpenger.model.marshalling.FaktumTilJsonHjelper.leggTilGyldigeLand
import no.nav.dagpenger.model.marshalling.FaktumTilJsonHjelper.leggTilLandGrupper
import no.nav.dagpenger.model.marshalling.SøkerJsonBuilder.ReadOnlyStrategy
import no.nav.dagpenger.model.seksjon.Seksjon
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.model.visitor.FaktumVisitor
import no.nav.dagpenger.model.visitor.SøknadprosessVisitor
import java.time.LocalDateTime
import java.util.UUID

class SøkerJsonBuilder(private val søknadprosess: Søknadprosess) : SøknadprosessVisitor {
    companion object {
        private val mapper = ObjectMapper()
        private val skalIkkeBesvaresAvSøker = ReadOnlyStrategy { it.harIkkeRolle(Rolle.søker) }
    }

    private val root: ObjectNode = mapper.createObjectNode()
    private val seksjoner = mapper.createArrayNode()
    private lateinit var gjeldendeFakta: Seksjon
    private lateinit var avhengigheter: MutableSet<Faktum<*>>
    private val ferdig = søknadprosess.erFerdigFor(Rolle.søker, Rolle.nav)
    private val avhengigeFaktaErLåst = ReadOnlyStrategy {
        !gjeldendeFakta.contains(it)
    }

    init {
        søknadprosess.accept(this)
    }

    fun resultat() = root

    override fun preVisit(søknad: Søknad, prosessVersjon: Prosessversjon, uuid: UUID) {
        root.put("@event_name", "søker_oppgave")
        root.put("versjon_id", prosessVersjon.versjon)
        root.put("versjon_navn", prosessVersjon.prosessnavn.id)
        root.put("@opprettet", "${LocalDateTime.now()}")
        root.put("@id", "${UUID.randomUUID()}")
        root.put("søknad_uuid", "$uuid")
        root.put("ferdig", ferdig)
    }

    override fun visit(type: Identer.Ident.Type, id: String, historisk: Boolean) {
        if (type == Identer.Ident.Type.FOLKEREGISTERIDENT) {
            root.put("fødselsnummer", id)
        }
    }

    override fun postVisit(søknad: Søknad, prosessVersjon: Prosessversjon, uuid: UUID) {
        root.set<ArrayNode>("seksjoner", seksjoner)
    }

    override fun preVisit(seksjon: Seksjon, rolle: Rolle, fakta: Set<Faktum<*>>, indeks: Int) {
        avhengigheter = mutableSetOf()
        gjeldendeFakta = seksjon.gjeldendeFakta(søknadprosess.rootSubsumsjon)
    }

    override fun postVisit(seksjon: Seksjon, rolle: Rolle, indeks: Int) {
        if (rolle != Rolle.søker) return
        if (gjeldendeFakta.isEmpty()) return
        // Splitt grunnleggende fakta og generatorer sine fakta
        val avhengigheterUtenforGjeldendeSeksjon = avhengigheter.filterNot { gjeldendeFakta.contains(it) }
        val (fakta, generatorFakta) = (avhengigheterUtenforGjeldendeSeksjon + gjeldendeFakta).partition { !it.faktumId.harIndeks() }
        val faktaNode = fakta.fold(mapper.createArrayNode()) { acc, faktum ->
            val generatorFaktumFakta = when (faktum) {
                is GeneratorFaktum -> generatorFakta.filter { faktum.harGenerert(it.faktumId) }.toSet()
                else -> emptySet()
            }

            acc.add(SøknadFaktumVisitor(faktum, generatorFaktumFakta, avhengigeFaktaErLåst).root)
        }

        mapper.createObjectNode().apply {
            put("beskrivendeId", seksjon.navn)
            put("ferdig", gjeldendeFakta.erAlleBesvart())

            set<ArrayNode>("fakta", faktaNode)
            seksjoner.add(this)
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
        if (!::gjeldendeFakta.isInitialized) return
        avhengigheter.addAll(genererteFaktum + faktum)
    }

    override fun <R : Comparable<R>> postVisitAvhengerAvFakta(
        faktum: Faktum<R>,
        avhengerAvFakta: MutableSet<Faktum<*>>
    ) {
        if (!::gjeldendeFakta.isInitialized) return
        if (!gjeldendeFakta.contains(faktum)) return
        avhengigheter.addAll(avhengerAvFakta)
    }

    private fun interface ReadOnlyStrategy {
        fun readOnly(faktum: Faktum<*>): Boolean
    }

    private class SøknadFaktumVisitor(
        faktum: Faktum<*>,
        private val besvarteOgNesteGeneratorFakta: Set<Faktum<*>> = emptySet(),
        private val readOnlyStrategy: ReadOnlyStrategy = skalIkkeBesvaresAvSøker
    ) : FaktumVisitor {
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
            this.root.lagFaktumNode<R>(id, clazz.simpleName.lowercase(), faktum.navn, roller, null, gyldigeValg)
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
            val jsonTemplates = mapper.createArrayNode()
            templates.forEach { template ->
                jsonTemplates.add(SøknadFaktumVisitor(template).root)
            }
            this.root.lagFaktumNode<R>(id, "generator", faktum.navn, roller, jsonTemplates)
            this.root.put("readOnly", readOnlyStrategy.readOnly(faktum))
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
            val jsonTemplates = templates.fold(mapper.createArrayNode()) { acc, template ->
                acc.add(SøknadFaktumVisitor(template).root)
            }

            this.root.lagFaktumNode(id, "generator", faktum.navn, roller, jsonTemplates, svar = svar)
            val svarListe = mapper.createArrayNode().apply {
                // Skal alltid inneholde like mange elementer som generatoren er besvart med
                repeat(faktum.svar()) { i -> insertArray(i) }
                // Grupper fakta etter indeks så de kan slås opp i
                val grupperteFakta =
                    besvarteOgNesteGeneratorFakta.groupBy { it.faktumId.reflection { _, indeks -> indeks } }
                // Erstatt placeholdere med besvarte eller neste fakta
                forEachIndexed { indeks, arrayNode ->
                    set(
                        indeks,
                        grupperteFakta[indeks + 1]?.fold(mapper.createArrayNode()) { acc, faktum ->
                            acc.add(SøknadFaktumVisitor(faktum, readOnlyStrategy = readOnlyStrategy).root)
                        } ?: arrayNode
                    )
                }
            }

            this.root.set<ArrayNode>("svar", svarListe)
            this.root.put("readOnly", readOnlyStrategy.readOnly(faktum))
        }

        override fun <R : Comparable<R>> visitMedSvar(
            faktum: GrunnleggendeFaktum<R>,
            tilstand: Faktum.FaktumTilstand,
            id: String,
            avhengigeFakta: Set<Faktum<*>>,
            avhengerAvFakta: Set<Faktum<*>>,
            godkjenner: Set<Faktum<*>>,
            sannsynliggjøringsFakta: Set<Faktum<*>>,
            roller: Set<Rolle>,
            clazz: Class<R>,
            svar: R,
            besvartAv: String?,
            gyldigeValg: GyldigeValg?,
            landGrupper: LandGrupper?
        ) {
            var overstyrbareGyldigeValg = gyldigeValg
            if (clazz.erBoolean()) {
                overstyrbareGyldigeValg = faktum.lagBeskrivendeIderForGyldigeBoolskeValg()
            }
            this.root.lagFaktumNode(
                id,
                clazz.simpleName.lowercase(),
                faktum.navn,
                roller,
                null,
                overstyrbareGyldigeValg,
                svar,
                besvartAv
            )
            if (clazz.erLand()) {
                this.root.leggTilGyldigeLand()
                this.root.leggTilLandGrupper(landGrupper)
            }

            this.root.put("readOnly", readOnlyStrategy.readOnly(faktum))
        }

        override fun <R : Comparable<R>> visitUtenSvar(
            faktum: GrunnleggendeFaktum<R>,
            tilstand: Faktum.FaktumTilstand,
            id: String,
            avhengigeFakta: Set<Faktum<*>>,
            avhengerAvFakta: Set<Faktum<*>>,
            godkjenner: Set<Faktum<*>>,
            sannsynliggjøringsFakta: Set<Faktum<*>>,
            roller: Set<Rolle>,
            clazz: Class<R>,
            gyldigeValg: GyldigeValg?,
            landGrupper: LandGrupper?
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
                overstyrbareGyldigeValg
            )

            if (clazz.erLand()) {
                this.root.leggTilGyldigeLand()
                this.root.leggTilLandGrupper(landGrupper)
            }
            this.root.put("readOnly", readOnlyStrategy.readOnly(faktum))
        }
    }
}
