package no.nav.dagpenger.quiz.mediator.behovløsere

import java.time.LocalDate

// Forstår terskelverdier for minste arbeidsinntekt
object MinstearbeidsinntektFaktorStrategi {

    private val terskler = listOf(
        MinstearbeidsinntektTerskel(
            20.mars(2020),
            30.oktober(2020),
            Faktor(0.75, 2.25)
        ), // Forskrift § 2-2.Midlertidig krav til minsteinntekt – unntak fra folketrygdloven § 4-4
        MinstearbeidsinntektTerskel(
            19.februar(2021),
            30.september(2021),
            Faktor(0.75, 2.25)
        ),
        MinstearbeidsinntektTerskel(
            19.februar(2021),
            30.september(2021),
            Faktor(0.75, 2.25)
        ), // Forskrift § 2-2.Midlertidig krav til minsteinntekt – unntak fra folketrygdloven § 4-4
        MinstearbeidsinntektTerskel(
            15.desember(2021),
            28.februar(2022),
            Faktor(0.75, 2.25)
        ),
        MinstearbeidsinntektTerskel(
            LocalDate.MIN,
            LocalDate.MAX,
            Faktor(1.5, 3.0)
        ) // https://lovdata.no/lov/1997-02-28-19/§4-4
    )

    fun finnFaktor(virkningsdato: LocalDate): Faktor {
        return terskler.first { virkningsdato in it }.faktor
    }

    private class MinstearbeidsinntektTerskel constructor(
        private val fom: LocalDate,
        private val tom: LocalDate,
        val faktor: Faktor
    ) : ClosedRange<LocalDate> {

        init {
            require(fom.isBefore(tom)) { "Til og med '$fom' må være før Fra-og-med '$tom'" }
        }

        override val endInclusive: LocalDate
            get() = tom
        override val start: LocalDate
            get() = fom
    }

    data class Faktor(
        val nedre: Double,
        val øvre: Double
    )
}

private fun Int.januar(year: Int) = LocalDate.of(year, 1, this)
private fun Int.februar(year: Int) = LocalDate.of(year, 2, this)
private fun Int.mars(year: Int) = LocalDate.of(year, 3, this)
private fun Int.april(year: Int) = LocalDate.of(year, 4, this)
private fun Int.mai(year: Int) = LocalDate.of(year, 5, this)
private fun Int.juni(year: Int) = LocalDate.of(year, 6, this)
private fun Int.juli(year: Int) = LocalDate.of(year, 7, this)
private fun Int.august(year: Int) = LocalDate.of(year, 8, this)
private fun Int.september(year: Int) = LocalDate.of(year, 9, this)
private fun Int.oktober(year: Int) = LocalDate.of(year, 10, this)
private fun Int.november(year: Int) = LocalDate.of(year, 11, this)
private fun Int.desember(year: Int) = LocalDate.of(year, 12, this)
