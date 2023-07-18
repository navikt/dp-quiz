package no.nav.dagpenger.quiz.mediator.land

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.dagpenger.model.faktum.Land
import java.io.FileNotFoundException

data class LandKode(private val id: Int, val alpha3Code: String)
internal object LandOppslag {

    /**
     * world.json hentet fra https://github.com/stefangabos/world_countries
     */
    private const val WORLD_JSON = "/world.json"
    private val mapper = jacksonObjectMapper()

    private val world by lazy {
        this.javaClass.getResource(WORLD_JSON)?.openStream()?.buffered()?.reader()?.use {
            it.readText()
        } ?: throw FileNotFoundException("Fant ikke filen $WORLD_JSON")
    }

    val land = world.let { mapper.readTree(it) }.map { Land(it["alpha3"].asText().uppercase()) }.toSet()
}
