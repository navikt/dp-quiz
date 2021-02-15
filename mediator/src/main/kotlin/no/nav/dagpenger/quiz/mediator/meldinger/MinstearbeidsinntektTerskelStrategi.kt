package no.nav.dagpenger.quiz.mediator.meldinger

import java.lang.IllegalArgumentException
import java.time.LocalDate

object MinsteArbeidsinntektStrategi {
    data class Terskel(
        val nedre: Double,
        val øvre: Double
    )
    private val terskler = mutableListOf<MinsteArbeidsinntektStrategi.MinstearbeidsinntektTerskelStrategi>()

    private val ordinær = MinstearbeidsinntektTerskelStrategi(LocalDate.MIN, LocalDate.MAX, Terskel(1.5, 3.0))

    private class MinstearbeidsinntektTerskelStrategi constructor(
        private val fom: LocalDate,
        private val tom: LocalDate,
        val terskel: Terskel
    ) : ClosedRange<LocalDate> {

        init {
            terskler.add(this)
        }

        override val endInclusive: LocalDate
            get() = tom
        override val start: LocalDate
            get() = fom
    }

    fun finnTerskel(virkningstidspunkt: LocalDate): Terskel {
        return terskler.find { virkningstidspunkt in it }?.terskel
            ?: throw IllegalArgumentException("Fant ikke terskel basert på virkningstidspunkt $virkningstidspunkt")
    }
}
