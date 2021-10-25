package no.nav.dagpenger.model.marshalling

import no.nav.dagpenger.model.seksjon.Søknadprosess
import java.util.Locale

class SøkerJsonBuilder(søknadprosess: Søknadprosess, seksjonNavn: String, lokal: Locale = Språk.bokmål) :
    SøknadJsonBuilder(lokal = lokal) {

}
