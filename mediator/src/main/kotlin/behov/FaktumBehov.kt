package behov


// Forstår hvordan en kan gå fra faktum til behov for en spesifikk versjon
internal class FaktumBehov(versjon: Int, private val delegate: Map<Int, String>) : Map<Int, String> by delegate {
    override operator fun get(key: Int): String = delegate[key] ?: throw IllegalArgumentException("Ukjent faktum id $key")

    init {
        versjoner[versjon] = this
    }

    companion object {
        private val versjoner = mutableMapOf<Int, FaktumBehov>()
        fun id(versjonId: Int) = requireNotNull(versjoner[versjonId]) { "Vet ikke om versjon $versjonId" }
    }
}