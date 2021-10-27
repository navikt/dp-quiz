package no.nav.dagpenger.model.marshalling

import com.fasterxml.jackson.databind.node.ObjectNode
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

        root.put("saksbehandles_på_ekte", søknadprosess.saksbehandlesPåEkte())
    }

    override fun resultat(): ObjectNode {
        if (søknadprosess.resultat() == null) throw IllegalStateException("Kan ikke lage resultat av subsumsjonstre uten resultat")
        return super.resultat()
    }

    override fun preVisit(søknad: Søknad, versjonId: Int, uuid: UUID) {
        super.preVisit(søknad, versjonId, uuid)
        root.put("@event_name", "prosess_resultat")
        root.put("@opprettet", "${LocalDateTime.now()}")
        root.put("@id", "${UUID.randomUUID()}")
        root.put("søknad_uuid", "$uuid")
        root.put("resultat", søknadprosess.resultat())
        root.set("identer", identerNode)
        root.set("fakta", faktaNode)
        root.set("subsumsjoner", subsumsjonRoot)
    }
}
