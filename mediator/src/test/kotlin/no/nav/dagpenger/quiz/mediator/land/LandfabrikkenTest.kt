package no.nav.dagpenger.quiz.mediator.land

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

internal class LandfabrikkenTest {
    private val fabrikken = Landfabrikken

    @Test
    fun validation() {
        assertThrows<IllegalArgumentException> { fabrikken.land("NORGE") }
        assertThrows<IllegalArgumentException> { fabrikken.land("DDD") }
    }

    @Test
    fun `Må tillate PDL sin kode for ukjent land`() {
        assertDoesNotThrow { fabrikken.land("XUK") }
        assertDoesNotThrow { fabrikken.land("xuk") }
        assertDoesNotThrow { fabrikken.land("xUk") }
    }

    @Test
    fun `Må tillate PDL sin kode for statsløs`() {
        assertDoesNotThrow { fabrikken.land("XXX") }
        assertDoesNotThrow { fabrikken.land("xxx") }
        assertDoesNotThrow { fabrikken.land("xXx") }
    }

    @Test
    fun `Må tillate den uoffisielle koden for Kosovo, siden PDL kan returnere den`() {
        assertDoesNotThrow { fabrikken.land("XXK") }
        assertDoesNotThrow { fabrikken.land("xxk") }
        assertDoesNotThrow { fabrikken.land("xXk") }
    }
}
