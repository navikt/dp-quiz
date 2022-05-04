package no.nav.dagpenger.model.marshalling

import com.fasterxml.jackson.databind.node.ArrayNode
import no.nav.dagpenger.model.factory.FaktaRegel
import no.nav.dagpenger.model.faktum.Faktum
import no.nav.dagpenger.model.faktum.GeneratorFaktum
import no.nav.dagpenger.model.faktum.GrunnleggendeFaktum
import no.nav.dagpenger.model.faktum.Prosessversjon
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.faktum.UtledetFaktum
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.model.subsumsjon.GodkjenningsSubsumsjon
import no.nav.dagpenger.model.subsumsjon.Subsumsjon
import java.time.LocalDateTime
import java.util.UUID

class SaksbehandlerJsonBuilder(
    private val søknadprosess: Søknadprosess,
    private val seksjonNavn: String,
    private val indeks: Int = 0
) : SøknadJsonBuilder() {
    private val relevanteFakta = mutableSetOf<String>()
    private val genererteFakta = mutableSetOf<Faktum<*>>()

    init {
        søknadprosess.søknad.accept(this)
        søknadprosess.rootSubsumsjon.mulige().accept(this)
        søknadprosess.first { seksjonNavn == it.navn && indeks == it.indeks }
            .filtrertSeksjon(søknadprosess.rootSubsumsjon).accept(this)
        ignore = false
        genererteFakta.forEach { it.accept(this) }
    }

    override fun preVisit(søknad: Søknad, prosessVersjon: Prosessversjon, uuid: UUID) {
        super.preVisit(søknad, prosessVersjon, uuid)
        root.put("@event_name", "oppgave")
        root.put("@opprettet", "${LocalDateTime.now()}")
        root.put("@id", "${UUID.randomUUID()}")
        root.put("søknad_uuid", "$uuid")
        root.put("seksjon_navn", seksjonNavn)
        root.put("indeks", indeks)
        root.set<ArrayNode>("identer", identerNode)
        root.set<ArrayNode>("fakta", faktaNode)
        root.set<ArrayNode>("subsumsjoner", subsumsjonRoot)
    }

    override fun preVisit(
        subsumsjon: GodkjenningsSubsumsjon,
        action: GodkjenningsSubsumsjon.Action,
        godkjenning: List<GrunnleggendeFaktum<Boolean>>,
        lokaltResultat: Boolean?,
        childResultat: Boolean?
    ) {
        relevanteFakta += godkjenning.map { it.id }
        super.preVisit(subsumsjon, action, godkjenning, lokaltResultat, childResultat)
    }

    override fun putSubsumsjon(
        lokaltResultat: Boolean?,
        subsumsjon: Subsumsjon,
        type: String
    ) {
        relevanteFakta += subsumsjon.alleFakta().map { it.id }
        super.putSubsumsjon(lokaltResultat, subsumsjon, type)
    }

    override fun <R : Comparable<R>> lagFaktumNode(
        id: String,
        navn: String,
        roller: Set<Rolle>,
        godkjenner: Set<Faktum<*>>,
        type: Class<R>,
        svar: R?,
        besvartAv: String?
    ) {
        if (id !in relevanteFakta) return
        super.lagFaktumNode(id, navn, roller, godkjenner, type, svar, besvartAv)
    }

    override fun <R : Comparable<R>> preVisit(
        faktum: UtledetFaktum<R>,
        id: String,
        avhengigeFakta: Set<Faktum<*>>,
        avhengerAvFakta: Set<Faktum<*>>,
        children: Set<Faktum<*>>,
        clazz: Class<R>,
        regel: FaktaRegel<R>,
        svar: R
    ) {
        relevanteFakta += children.map { it.id }
        super.preVisit(faktum, id, avhengigeFakta, avhengerAvFakta, children, clazz, regel, svar)
    }

    override fun <R : Comparable<R>> visitMedSvar(
        faktum: GeneratorFaktum,
        id: String,
        avhengigeFakta: Set<Faktum<*>>,
        avhengerAvFakta: Set<Faktum<*>>,
        templates: List<Faktum<*>>,
        roller: Set<Rolle>,
        clazz: Class<R>,
        svar: R
    ) {
        if (!ignore) {
            val genererte = søknadprosess.flatMap {
                it.filter { faktum ->
                    templates.any { template ->
                        faktum.faktumId.generertFra(template.faktumId)
                    }
                }
            }
            relevanteFakta += genererte.map { it.id }
            genererteFakta += genererte
        }
    }
}
