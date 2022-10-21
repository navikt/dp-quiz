package no.nav.dagpenger.model.faktum

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.io.FileNotFoundException

data class LandKode(private val id: Int, val alpha3Code: String)

internal object LandOppslag {

    /**
     * world.json hentet fra https://github.com/stefangabos/world_countries
     */
    private val WORLD_JSON = "/world.json"

    private val mapper = jacksonObjectMapper()

    private val world = this.javaClass.getResource(WORLD_JSON)?.openStream()?.buffered()?.reader()?.use {
        it.readText()
    } ?: throw FileNotFoundException("Fant ikke filen $WORLD_JSON")

    private val landkoder =
        world.let { mapper.readTree(it) }.map { LandKode(it["id"].asInt(), it["alpha3"].asText().uppercase()) }.toSet()

    fun land(): Set<LandKode> = landkoder
    fun fraAlpha3Code(alpha3Code: String): LandKode? = landkoder.find { alpha3Code.equals(it.alpha3Code, true) }
}
