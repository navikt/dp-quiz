package no.nav.dagpenger.model.unit.faktum

import no.nav.dagpenger.model.faktum.Person
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class PersonTest {

    @Test
    fun ` personer er unike `() {
        val person1 = Person("123456789011", "aktørId")
        val person2 = Person("123456789011", "aktørId")
        val person3 = Person("112345678902", "aktørId")
        assertEquals(person1, person2)
        assertEquals(hashSetOf(person1), hashSetOf(person2))
        assertNotEquals(hashSetOf(person1), hashSetOf(person3))
    }
}
