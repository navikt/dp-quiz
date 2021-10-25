package no.nav.dagpenger.quiz.mediator.meldinger

import no.nav.dagpenger.quiz.mediator.db.SøknadRecord
import no.nav.helse.rapids_rivers.RapidsConnection

class GjenopptakService(søknadPersistence: SøknadRecord, rapidsConnection: RapidsConnection, versjonId: Int)
