package no.nav.dagpenger.model.marshalling

import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.marshalling.Språk.Companion.bokmål
import no.nav.dagpenger.model.seksjon.Søknadprosess
import java.time.LocalDateTime
import java.util.Locale
import java.util.UUID

class SaksbehandlerJsonBuilder(
    søknadprosess: Søknadprosess,
    private val seksjonNavn: String,
    private val indeks: Int = 0,
    lokal: Locale = bokmål
) : SøknadJsonBuilder(lokal = lokal) {

    init {
        søknadprosess.søknad.accept(this)
        søknadprosess.rootSubsumsjon.mulige().accept(this)
        søknadprosess.first { seksjonNavn == it.navn && indeks == it.indeks }
            .filtrertSeksjon(søknadprosess.rootSubsumsjon).accept(this)
    }

    override fun preVisit(søknad: Søknad, versjonId: Int, uuid: UUID) {
        super.preVisit(søknad, versjonId, uuid)
        root.put("@event_name", "oppgave")
        root.put("@opprettet", "${LocalDateTime.now()}")
        root.put("@id", "${UUID.randomUUID()}")
        root.put("søknad_uuid", "$uuid")
        root.put("seksjon_navn", seksjonNavn)
        root.put("indeks", indeks)
        root.set("identer", identerNode)
        root.set("fakta", faktaNode)
        root.set("subsumsjoner", subsumsjonRoot)
    }
}
