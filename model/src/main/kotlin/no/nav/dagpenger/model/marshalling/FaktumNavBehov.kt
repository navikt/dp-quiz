package no.nav.dagpenger.model.marshalling

// Forstår hvordan en kan gå fra faktum til behov for en spesifikk versjon
open class FaktumNavBehov(private val delegate: Map<Int, String> = emptyMap()) : Map<Int, String> by delegate {
    override operator fun get(key: Int): String =
        delegate[key] ?: throw IllegalArgumentException("Fant ikke faktum for faktumid: $key")
}
