package no.nav.dagpenger.model.marshalling

// Forstår hvordan en kan gå fra faktum til behov for en spesifikk versjon
class FaktumNavBehov(versjon: Int, private val delegate: Map<Int, String>) : Map<Int, String> by delegate {
    override operator fun get(key: Int): String = delegate[key] ?: throw IllegalArgumentException("Ukjent faktum id $key")

    init {
        require(versjon !in versjoner.keys) { "Ugyldig forsøk på å opprette duplikat FaktumNavBehov versjon: $versjon" }
        versjoner[versjon] = this
    }

    companion object {
        private val versjoner = mutableMapOf<Int, FaktumNavBehov>()
        fun id(versjonId: Int) = requireNotNull(versjoner[versjonId]) { "Vet ikke om versjon $versjonId" }

        val siste: FaktumNavBehov
            get() = versjoner.maxByOrNull { it.key }?.value
                ?: throw IllegalArgumentException("Det finnes ingen versjoner!")
    }
}
