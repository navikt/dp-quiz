import no.nav.helse.rapids_rivers.RapidsConnection

class NavMediator(rapidsConnection: RapidsConnection) {

    init {
        rapidsConnection.apply {
            MinsteinntektRiver(this)
            MinsteinntektLøsningRiver(this)
            VernepliktRiver(this)
            VernepliktLøsningRiver(this)
        }
    }
}
