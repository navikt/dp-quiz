package no.nav.dagpenger.model.marshalling

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import no.nav.dagpenger.model.faktum.Faktum
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

class SøkerJsonBuilder(søknadprosess: Søknadprosess) : SøknadprosessVisitor {
    companion object {
        private val mapper = ObjectMapper()
        private val iSeksjonReadOnlyStrategy = ReadOnlyStrategy { it.harIkkeRolle(Rolle.søker) }
    }

    private val root: ObjectNode = mapper.createObjectNode()
    private val seksjoner = mapper.createArrayNode()
    private lateinit var gjeldendeSeksjon: ObjectNode
    private var besøkteFaktum: MutableList<BesøktFaktum> = mutableListOf()
    private lateinit var seksjonFakta: Set<Faktum<*>>
    private lateinit var seksjonAvhengerAvFakta: Set<Faktum<*>>
    private val nesteUbesvarteFakta = søknadprosess.nesteFakta()
    private val erGenerertFraTemplate = mutableListOf<Faktum<*>>()
    private val ferdig = søknadprosess.erFerdigFor(Rolle.søker, Rolle.nav)

    init {
        søknadprosess.søknad.accept(this)
        søknadprosess.forEach { seksjon -> seksjon.accept(this) }
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
        val besvarteFakta = fakta.filter { it.erBesvart() }.toSet()
        val erAlleBesvart = besvarteFakta.isNotEmpty()
        val nesteFakta = nesteUbesvarteFakta.any { fakta.contains(it) }

        seksjonFakta = if (erAlleBesvart || nesteFakta) {
            if (nesteFakta) {
                besvarteFakta + nesteUbesvarteFakta
            } else {
                fakta
            }
        } else {
            emptySet()
        }
    }

    override fun preVisitAvhengerAv(seksjon: Seksjon, avhengerAvFakta: Set<Faktum<*>>) {
        seksjonAvhengerAvFakta = avhengerAvFakta.filter { it.erBesvart() }.toSet()
    }

    override fun postVisitAvhengerAv(seksjon: Seksjon, avhengerAvFakta: Set<Faktum<*>>) {
    }

    override fun postVisit(seksjon: Seksjon, rolle: Rolle, indeks: Int) {
        if (rolle != Rolle.søker) return
        gjeldendeSeksjon = mapper.createObjectNode().apply {
            put("beskrivendeId", seksjon.navn)
        }

        val avhengerReadOnlyStrategy = ReadOnlyStrategy {
            !seksjonFakta.contains(it) || !seksjonFakta.any { seksjonFaktum ->
                seksjonFaktum.faktumId.generertFra(it.faktumId)
            }
        }
        if (seksjonFakta.isNotEmpty()) {
            val ubesvarteFakta = nesteUbesvarteFakta.filter { seksjonFakta.contains(it) }
            val faktumSomSkalMed = seksjonFakta + seksjonAvhengerAvFakta +  ubesvarteFakta
            val brukteFaktum = besøkteFaktum.filter { faktumSomSkalMed.contains(it.faktum) }
            val fakta =
                brukteFaktum.fold(mapper.createArrayNode()) { acc, (faktum, genererteFakta) ->
                    val strategi = when (faktum) {
                        in seksjonFakta -> iSeksjonReadOnlyStrategy
                        in seksjonAvhengerAvFakta -> avhengerReadOnlyStrategy
                        else -> iSeksjonReadOnlyStrategy
                    }
                    acc.add(SøknadFaktumVisitor(faktum, genererteFakta, readOnlyStrategy = strategi).root)
                }
            gjeldendeSeksjon.set<ArrayNode>("fakta", fakta)
            seksjoner.add(gjeldendeSeksjon)
        }
        seksjonAvhengerAvFakta = mutableSetOf()
    }

    override fun <R : Comparable<R>> visitMedSvar(
        faktum: GeneratorFaktum,
        id: String,
        avhengigeFakta: Set<Faktum<*>>,
        avhengerAvFakta: Set<Faktum<*>>,
        templates: List<Faktum<*>>,
        roller: Set<Rolle>,
        clazz: Class<R>,
        svar: R,
        genererteFaktum: Set<Faktum<*>>
    ) {
        // TODO: Finn ut hvordan vi kan hente svar i generator når de inkluderes som avhengigeAv
        val genererte = erGenerertFraTemplate.filter { generertFaktum ->
            templates.any { generertFaktum.faktumId.generertFra(it.faktumId) }
        }.toSet()

        addFaktum(faktum, genererte)
    }

    override fun <R : Comparable<R>> visitUtenSvar(
        faktum: GeneratorFaktum,
        id: String,
        avhengigeFakta: Set<Faktum<*>>,
        avhengerAvFakta: Set<Faktum<*>>,
        templates: List<Faktum<*>>,
        roller: Set<Rolle>,
        clazz: Class<R>,
    ) {
        if (faktum !in nesteUbesvarteFakta) return
        addFaktum(faktum)
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
        landGrupper: LandGrupper?,
    ) {
        if (faktum.faktumId.harIndeks()) {
            erGenerertFraTemplate.add(faktum)
            return
        }

        addFaktum(faktum)
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
        if (faktum !in nesteUbesvarteFakta) return
        if (faktum.faktumId.harIndeks()) {
            erGenerertFraTemplate.add(faktum)
            return
        }
        addFaktum(faktum)
    }

    private fun <R : Comparable<R>> addFaktum(
        faktum: Faktum<R>,
        genererteFakta: Set<Faktum<*>> = emptySet(),
    ) {
        if (besøkteFaktum.any { it.faktum == faktum }) return
        besøkteFaktum.add(
            BesøktFaktum(
                faktum,
                genererteFakta,
            )
        )
    }

    private fun interface ReadOnlyStrategy {
        fun readOnly(faktum: Faktum<*>): Boolean
    }

    private class SøknadFaktumVisitor(
        faktum: Faktum<*>,
        private val genererteFaktum: Set<Faktum<*>> = emptySet(),
        private val readOnlyStrategy: ReadOnlyStrategy = iSeksjonReadOnlyStrategy
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
            gyldigeValg: GyldigeValg?,
        ) {
            this.root.lagFaktumNode<R>(id, clazz.simpleName.lowercase(), faktum.navn, roller, null, gyldigeValg)
        }

        override fun <R : Comparable<R>> visitUtenSvar(
            faktum: GeneratorFaktum,
            id: String,
            avhengigeFakta: Set<Faktum<*>>,
            avhengerAvFakta: Set<Faktum<*>>,
            templates: List<Faktum<*>>,
            roller: Set<Rolle>,
            clazz: Class<R>,
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
            templates: List<Faktum<*>>,
            roller: Set<Rolle>,
            clazz: Class<R>,
            svar: R,
            genererteFaktum: Set<Faktum<*>>
        ) {
            val jsonTemplates = mapper.createArrayNode()
            templates.forEach { template ->
                jsonTemplates.add(SøknadFaktumVisitor(template, readOnlyStrategy = readOnlyStrategy).root)
            }
            this.root.lagFaktumNode(id, "generator", faktum.navn, roller, jsonTemplates, svar = svar)
            val svarListe = mapper.createArrayNode()

            (1..faktum.svar()).forEach { i ->
                val indeks = mapper.createArrayNode()
                this.genererteFaktum.filter { faktum ->
                    faktum.faktumId.reflection { _, indeks ->
                        indeks == i
                    }
                }.forEach {
                    indeks.add(SøknadFaktumVisitor(it, readOnlyStrategy = readOnlyStrategy).root)
                }
                svarListe.add(indeks)
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
            landGrupper: LandGrupper?,
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
                overstyrbareGyldigeValg
            )

            if (clazz.erLand()) {
                this.root.leggTilGyldigeLand()
                this.root.leggTilLandGrupper(landGrupper)
            }
            this.root.put("readOnly", readOnlyStrategy.readOnly(faktum))
        }
    }

    // Representerer alle faktum har blitt besvart eller skal besvares i neste seksjon + avhengigheter
    private data class BesøktFaktum(
        val faktum: Faktum<*>,
        val genererteFaktum: Set<Faktum<*>>,
    )
}
