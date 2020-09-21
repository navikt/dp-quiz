package no.nav.dagpenger.model.visitor

import no.nav.dagpenger.model.søknad.Seksjon
import no.nav.dagpenger.model.søknad.Søknad
import java.util.UUID

interface SøknadVisitor {
    fun preVisit(søknad: Søknad, uuid: UUID) {}
    fun postVisit(søknad: Søknad) {}
    fun preVisit(seksjon: Seksjon) {}
    fun postVisit(seksjon: Seksjon) {}
}
