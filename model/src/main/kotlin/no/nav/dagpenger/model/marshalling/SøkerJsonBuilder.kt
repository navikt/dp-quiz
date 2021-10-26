package no.nav.dagpenger.model.marshalling

import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.seksjon.Søknadprosess
import java.time.LocalDateTime
import java.util.Locale
import java.util.UUID

class SøkerJsonBuilder(
    søknadprosess: Søknadprosess,
    seksjonNavn: String,
    private val indeks: Int = 0,
    lokal: Locale = Språk.bokmål
) : SøknadJsonBuilder(lokal = lokal) {

    init {
        søknadprosess.søknad.accept(this)
        søknadprosess.rootSubsumsjon.mulige().accept(this)
        søknadprosess.first { seksjonNavn == it.navn && indeks == it.indeks }
            .filtrertSeksjon(søknadprosess.rootSubsumsjon).accept(this)
        ignore = false
    }

    override fun preVisit(søknad: Søknad, versjonId: Int, uuid: UUID) {
        super.preVisit(søknad, versjonId, uuid)
        root.put("@event_name", "søker-oppgave")
        root.put("@id", "${UUID.randomUUID()}")
        root.put("@opprettet", "${LocalDateTime.now()}")
    }
}