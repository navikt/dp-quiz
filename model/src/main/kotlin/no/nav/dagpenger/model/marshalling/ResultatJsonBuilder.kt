package no.nav.dagpenger.model.marshalling

import com.fasterxml.jackson.databind.node.ObjectNode
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.seksjon.Søknadprosess
import java.lang.IllegalStateException
import java.time.LocalDateTime
import java.util.Locale
import java.util.UUID

class ResultatJsonBuilder(
    private val søknadprosess: Søknadprosess,
    språk: Locale = Oversetter.bokmål
) : SøknadJsonBuilder(språk) {

    init {
        søknadprosess.søknad.accept(this)
        søknadprosess.forEach { it.accept(this) }
        søknadprosess.rootSubsumsjon.mulige().accept(this)
    }

    override fun resultat(): ObjectNode {
        if (søknadprosess.resultat() == null) throw IllegalStateException("Kan ikke lage resultat av subsumsjonstre uten resultat")
        return super.resultat()
    }

    override fun preVisit(søknad: Søknad, versjonId: Int, uuid: UUID) {
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
