package no.nav.dagpenger.model.marshalling

import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import no.nav.dagpenger.model.faktum.Fakta
import no.nav.dagpenger.model.faktum.Faktaversjon
import no.nav.dagpenger.model.seksjon.Prosess
import java.time.LocalDateTime
import java.util.UUID

class ResultatJsonBuilder(
    private val prosess: Prosess,
) : FaktaJsonBuilder() {

    init {
        prosess.fakta.accept(this)
        prosess.rootSubsumsjon.mulige().accept(this)

        ignore = false
        prosess.fakta.forEach { if (it.erBesvart()) it.accept(this) }
    }

    override fun resultat(): ObjectNode {
        if (prosess.resultat() == null) throw IllegalStateException("Kan ikke lage resultat av subsumsjonstre uten resultat")
        return super.resultat()
    }

    override fun preVisit(fakta: Fakta, faktaversjon: Faktaversjon, uuid: UUID, navBehov: FaktumNavBehov) {
        super.preVisit(fakta, faktaversjon, uuid, navBehov)
        root.put("@event_name", "prosess_resultat")
        root.put("@opprettet", "${LocalDateTime.now()}")
        root.put("@id", "${UUID.randomUUID()}")
        root.put("versjon_id", faktaversjon.versjon)
        root.put("versjon_navn", faktaversjon.faktatype.id)
        root.put("søknad_uuid", "$uuid")
        root.put("resultat", prosess.resultat())
        root.set<ArrayNode>("identer", identerNode)
        root.set<ArrayNode>("fakta", faktaNode)
        root.set<ArrayNode>("subsumsjoner", subsumsjonRoot)
    }
}
