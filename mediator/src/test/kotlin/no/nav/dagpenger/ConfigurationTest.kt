package no.nav.dagpenger

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ConfigurationTest {
    @Test
    fun `check that our configuration behaves as expected`() {
        assertEquals("dp-quiz", config[application.name])
        assertEquals("8080", config[application.port].toString())
        assertEquals("consumer-group-id", config[kafka.consumer_group_id].toString())

        config.asMap().also {
            assertEquals(8, it.size)
            assertTrue(it.containsKey("KAFKA_CONSUMER_GROUP_ID"))
        }
    }
}
