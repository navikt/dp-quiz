package no.nav.dagpenger.model.marshalling

import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import no.nav.dagpenger.model.faktum.Fakta
import no.nav.dagpenger.model.faktum.HenvendelsesType
import no.nav.dagpenger.model.seksjon.Faktagrupper
import java.time.LocalDateTime
import java.util.UUID

class ResultatJsonBuilder(
    private val faktagrupper: Faktagrupper,
) : SøknadJsonBuilder() {

    init {
        faktagrupper.fakta.accept(this)
        faktagrupper.rootSubsumsjon.mulige().accept(this)

        ignore = false
        faktagrupper.fakta.forEach { if (it.erBesvart()) it.accept(this) }
    }

    override fun resultat(): ObjectNode {
        if (faktagrupper.resultat() == null) throw IllegalStateException("Kan ikke lage resultat av subsumsjonstre uten resultat")
        return super.resultat()
    }

    override fun preVisit(fakta: Fakta, prosessVersjon: HenvendelsesType, uuid: UUID) {
        super.preVisit(fakta, prosessVersjon, uuid)
        root.put("@event_name", "prosess_resultat")
        root.put("@opprettet", "${LocalDateTime.now()}")
        root.put("@id", "${UUID.randomUUID()}")
        root.put("versjon_id", prosessVersjon.versjon)
        root.put("versjon_navn", prosessVersjon.prosessnavn.id)
        root.put("søknad_uuid", "$uuid")
        root.put("resultat", faktagrupper.resultat())
        root.set<ArrayNode>("identer", identerNode)
        root.set<ArrayNode>("fakta", faktaNode)
        root.set<ArrayNode>("subsumsjoner", subsumsjonRoot)
    }
}
