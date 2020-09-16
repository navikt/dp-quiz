package no.nav.dagpenger.søknad

import no.nav.dagpenger.model.søknad.Seksjon
import no.nav.dagpenger.model.søknad.Søknad
import no.nav.dagpenger.regelverk.fødselsdato
import no.nav.dagpenger.regelverk.ønsketDato

val datamaskin = Søknad(Seksjon(ønsketDato, fødselsdato))
