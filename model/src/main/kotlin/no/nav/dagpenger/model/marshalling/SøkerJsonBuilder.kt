package no.nav.dagpenger.model.marshalling

import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.dagpenger.model.faktum.Dokument
import no.nav.dagpenger.model.faktum.Fakta
import no.nav.dagpenger.model.faktum.Faktum
import no.nav.dagpenger.model.faktum.Faktum.Companion.erAlleBesvart
import no.nav.dagpenger.model.faktum.GeneratorFaktum
import no.nav.dagpenger.model.faktum.GrunnleggendeFaktum
import no.nav.dagpenger.model.faktum.GyldigeValg
import no.nav.dagpenger.model.faktum.HenvendelsesType
import no.nav.dagpenger.model.faktum.Identer
import no.nav.dagpenger.model.faktum.LandGrupper
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.TemplateFaktum
import no.nav.dagpenger.model.marshalling.FaktumTilJsonHjelper.erBoolean
import no.nav.dagpenger.model.marshalling.FaktumTilJsonHjelper.erLand
import no.nav.dagpenger.model.marshalling.FaktumTilJsonHjelper.lagBeskrivendeIderForGyldigeBoolskeValg
import no.nav.dagpenger.model.marshalling.FaktumTilJsonHjelper.lagFaktumNode
import no.nav.dagpenger.model.marshalling.FaktumTilJsonHjelper.leggTilGyldigeLand
import no.nav.dagpenger.model.marshalling.FaktumTilJsonHjelper.leggTilLandGrupper
import no.nav.dagpenger.model.marshalling.SøkerJsonBuilder.ReadOnlyStrategy
import no.nav.dagpenger.model.seksjon.Seksjon
import no.nav.dagpenger.model.seksjon.Seksjon.Companion.brukerSeksjoner
import no.nav.dagpenger.model.seksjon.Utredningsprosess
import no.nav.dagpenger.model.subsumsjon.SannsynliggjøringsSubsumsjon
import no.nav.dagpenger.model.subsumsjon.Subsumsjon
import no.nav.dagpenger.model.visitor.FaktumVisitor
import no.nav.dagpenger.model.visitor.SubsumsjonVisitor
import no.nav.dagpenger.model.visitor.SøknadprosessVisitor
import java.time.LocalDateTime
import java.util.UUID

class SøkerJsonBuilder(private val utredningsprosess: Utredningsprosess) : SøknadprosessVisitor {
    companion object {
        private val mapper = jacksonObjectMapper()
        private val skalIkkeBesvaresAvSøker = ReadOnlyStrategy { it.harIkkeRolle(Rolle.søker) }
    }

    private val sannsynliggjøringsFaktaListe: Set<Faktum<*>> =
        SannsynliggjøringsFaktaFinner(utredningsprosess.rootSubsumsjon).fakta
    private val root: ObjectNode = mapper.createObjectNode()
    private val seksjoner = mapper.createArrayNode()
    private val seksjonerTotalt = mutableSetOf<Seksjon>()
    private lateinit var gjeldendeFakta: Seksjon
    private lateinit var avhengigheter: MutableSet<Faktum<*>>
    private val generatorer: MutableSet<GeneratorFaktum> = mutableSetOf()
    private val ferdig = utredningsprosess.erFerdigFor(Rolle.søker, Rolle.nav)
    private val avhengigeFaktaErLåst = ReadOnlyStrategy {
        !gjeldendeFakta.contains(it)
    }

    init {
        utredningsprosess.accept(this)
    }

    fun resultat() = root

    override fun preVisit(fakta: Fakta, prosessVersjon: HenvendelsesType, uuid: UUID) {
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

    override fun postVisit(fakta: Fakta, uuid: UUID) {
        root.set<ArrayNode>("seksjoner", seksjoner)
    }

    override fun postVisit(utredningsprosess: Utredningsprosess) {
        root.put("antallSeksjoner", seksjonerTotalt.brukerSeksjoner().size)
    }

    override fun preVisit(seksjon: Seksjon, rolle: Rolle, fakta: Set<Faktum<*>>, indeks: Int) {
        avhengigheter = mutableSetOf()
        gjeldendeFakta = seksjon.gjeldendeFakta(utredningsprosess.rootSubsumsjon)
        seksjonerTotalt.add(seksjon)
    }

    override fun postVisit(seksjon: Seksjon, rolle: Rolle, indeks: Int) {
        if (rolle != Rolle.søker) return
        if (gjeldendeFakta.isEmpty()) return
        // Splitt grunnleggende fakta og generatorer sine fakta
        val avhengigheterUtenforGjeldendeSeksjon = avhengigheter.filterNot { seksjon.contains(it) }
        val (fakta, generatorFakta) = (avhengigheterUtenforGjeldendeSeksjon + gjeldendeFakta).partition { !it.faktumId.harIndeks() }
        val faktaNode = fakta.fold(mapper.createArrayNode()) { acc, faktum ->
            val generatorFaktumFakta = when (faktum) {
                is GeneratorFaktum -> generatorFakta.filter { faktum.harGenerert(it.faktumId) }.toSet()
                else -> emptySet()
            }

            acc.add(
                SøknadFaktumVisitor(
                    faktum,
                    generatorFaktumFakta,
                    avhengigeFaktaErLåst,
                    sannsynliggjøringsFaktaListe
                ).root
            )
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
        generatorer.add(faktum)
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

    private class SannsynliggjøringsFaktaFinner(subsumsjon: Subsumsjon) : SubsumsjonVisitor {
        val fakta = mutableSetOf<GrunnleggendeFaktum<*>>()

        init {
            subsumsjon.accept(this)
        }

        override fun postVisit(
            subsumsjon: SannsynliggjøringsSubsumsjon,
            sannsynliggjøringsFakta: GrunnleggendeFaktum<*>,
            lokaltResultat: Boolean?
        ) {
            if (lokaltResultat != true) return
            fakta.add(sannsynliggjøringsFakta)
        }
    }

    private fun interface ReadOnlyStrategy {
        fun readOnly(faktum: Faktum<*>): Boolean
    }

    private inner class SøknadFaktumVisitor(
        faktum: Faktum<*>,
        private val besvarteOgNesteGeneratorFakta: Set<Faktum<*>> = emptySet(),
        // TODO: Erstatte dette med noe decoratorish?
        private val readOnlyStrategy: ReadOnlyStrategy = skalIkkeBesvaresAvSøker,
        private val sannsynliggjøringsFaktaListe: Set<Faktum<*>> = emptySet()
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
                    besvarteOgNesteGeneratorFakta.groupBy { fooooo -> fooooo.faktumId.reflection { _, indeks -> indeks } }
                // Erstatt placeholdere med besvarte eller neste fakta
                forEachIndexed { indeks, arrayNode ->
                    set(
                        indeks,
                        grupperteFakta[indeks + 1]?.fold(mapper.createArrayNode()) { acc, faktum ->
                            acc.add(
                                SøknadFaktumVisitor(
                                    faktum,
                                    readOnlyStrategy = readOnlyStrategy,
                                    sannsynliggjøringsFaktaListe = sannsynliggjøringsFaktaListe
                                ).root
                            )
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
            roller: Set<Rolle>,
            clazz: Class<R>,
            svar: R,
            besvartAv: String?,
            gyldigeValg: GyldigeValg?,
            landGrupper: LandGrupper?
        ) {
            skrivFaktum(
                gyldigeValg = gyldigeValg,
                clazz = clazz,
                faktum = faktum,
                id = id,
                roller = roller,
                svar = svar,
                besvartAv = besvartAv,
                landGrupper = landGrupper,
                avhengigeFakta = avhengigeFakta
            )
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
            skrivFaktum(
                gyldigeValg = gyldigeValg,
                clazz = clazz,
                faktum = faktum,
                id = id,
                roller = roller,
                landGrupper = landGrupper,
                avhengigeFakta = avhengigeFakta
            )
        }

        private fun <R : Comparable<R>> skrivFaktum(
            gyldigeValg: GyldigeValg?,
            clazz: Class<R>,
            faktum: GrunnleggendeFaktum<R>,
            id: String,
            roller: Set<Rolle>,
            svar: R? = null,
            besvartAv: String? = null,
            landGrupper: LandGrupper?,
            avhengigeFakta: Set<Faktum<*>>
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
            val sannsynliggjøringerAsJson = avhengigeFakta.filter { it.type() == Dokument::class.java }
                .filter { sannsynliggjøringsFaktaListe.contains(it) }
                .fold(mapper.createArrayNode()) { acc, template ->
                    val fakta = SøknadFaktumVisitor(template, readOnlyStrategy = readOnlyStrategy).root

                    if (template.faktumId.harIndeks()) {
                        generatorer.single { it.harGenerert(template.faktumId) }.identitet(template.faktumId)?.let {
                            fakta.put(
                                "generertAv",
                                when (it.erBesvart()) {
                                    true -> it.svar().verdi
                                    false -> "Ubesvart"
                                }
                            )
                        }
                    }
                    acc.add(fakta)
                }
            this.root.set<ArrayNode>("sannsynliggjoresAv", sannsynliggjøringerAsJson)
            this.root.put("readOnly", readOnlyStrategy.readOnly(faktum))
        }
    }
}
