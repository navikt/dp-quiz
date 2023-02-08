package no.nav.dagpenger.model.marshalling

import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import no.nav.dagpenger.model.faktum.Fakta
import no.nav.dagpenger.model.faktum.HenvendelsesType
import no.nav.dagpenger.model.seksjon.Utredningsprosess
import java.time.LocalDateTime
import java.util.UUID

class ResultatJsonBuilder(
    private val utredningsprosess: Utredningsprosess,
) : FaktaJsonBuilder() {

    init {
        utredningsprosess.fakta.accept(this)
        utredningsprosess.rootSubsumsjon.mulige().accept(this)

        ignore = false
        utredningsprosess.fakta.forEach { if (it.erBesvart()) it.accept(this) }
    }

    override fun resultat(): ObjectNode {
        if (utredningsprosess.resultat() == null) throw IllegalStateException("Kan ikke lage resultat av subsumsjonstre uten resultat")
        return super.resultat()
    }

    override fun preVisit(fakta: Fakta, henvendelsesType: HenvendelsesType, uuid: UUID) {
        super.preVisit(fakta, henvendelsesType, uuid)
        root.put("@event_name", "prosess_resultat")
        root.put("@opprettet", "${LocalDateTime.now()}")
        root.put("@id", "${UUID.randomUUID()}")
        root.put("versjon_id", henvendelsesType.versjon)
        root.put("versjon_navn", henvendelsesType.prosessnavn.id)
        root.put("søknad_uuid", "$uuid")
        root.put("resultat", utredningsprosess.resultat())
        root.set<ArrayNode>("identer", identerNode)
        root.set<ArrayNode>("fakta", faktaNode)
        root.set<ArrayNode>("subsumsjoner", subsumsjonRoot)
    }
}
