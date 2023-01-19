package no.nav.dagpenger.model.marshalling

import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import no.nav.dagpenger.model.faktum.Prosessversjon
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.seksjon.Søknadprosess
import java.time.LocalDateTime
import java.util.UUID

class ResultatJsonBuilder(
    private val søknadprosess: Søknadprosess,
) : SøknadJsonBuilder() {

    init {
        søknadprosess.søknad.accept(this)
        søknadprosess.rootSubsumsjon.mulige().accept(this)

        ignore = false
        søknadprosess.søknad.forEach { if (it.erBesvart()) it.accept(this) }
    }

    override fun resultat(): ObjectNode {
        if (søknadprosess.resultat() == null) throw IllegalStateException("Kan ikke lage resultat av subsumsjonstre uten resultat")
        return super.resultat()
    }

    override fun preVisit(søknad: Søknad, prosessVersjon: Prosessversjon, uuid: UUID) {
        super.preVisit(søknad, prosessVersjon, uuid)
        root.put("@event_name", "prosess_resultat")
        root.put("@opprettet", "${LocalDateTime.now()}")
        root.put("@id", "${UUID.randomUUID()}")
        root.put("versjon_id", prosessVersjon.versjon)
        root.put("versjon_navn", prosessVersjon.prosessnavn.id)
        root.put("søknad_uuid", "$uuid")
        root.put("resultat", søknadprosess.resultat())
        root.set<ArrayNode>("identer", identerNode)
        root.set<ArrayNode>("fakta", faktaNode)
        root.set<ArrayNode>("subsumsjoner", subsumsjonRoot)
    }
}
