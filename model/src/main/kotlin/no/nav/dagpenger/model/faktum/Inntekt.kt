package no.nav.dagpenger.model.faktum

import kotlin.math.absoluteValue
import kotlin.math.roundToInt
import kotlin.math.roundToLong

class Inntekt private constructor(
    private val årlig: Double,
) : Comparable<Inntekt> {
    init {
        require(
            this.årlig !in
                listOf(
                    Double.POSITIVE_INFINITY,
                    Double.NEGATIVE_INFINITY,
                    Double.NaN,
                ),
        ) { "inntekt må være gyldig nummer" }
    }

    companion object {
        private const val EPSILON = 0.000000000001 // 15 decimal places

        private const val ARBEIDSDAGER_PER_ÅR = 260

        val Number.månedlig get() = Inntekt(this.toDouble() * 12)

        val Number.årlig get() = Inntekt(this.toDouble())

        val Number.daglig get() = Inntekt(this.toDouble() * ARBEIDSDAGER_PER_ÅR)

        val INGEN = 0.daglig

        internal fun List<Inntekt>.summer(): Inntekt = this.fold(INGEN) { acc, inntekt -> Inntekt(acc.årlig + inntekt.årlig) }

        internal fun List<Inntekt>.gjennomsnitt(): Inntekt {
            if (this.isEmpty()) return INGEN
            return this.summer() / this.size
        }
    }

    fun <R> reflection(block: (Double, Double, Double, Int) -> R) =
        block(
            årlig,
            tilMånedligDouble(),
            tilDagligDouble(),
            tilDagligInt(),
        )

    private fun tilDagligInt() = (rundTilDaglig().årlig / ARBEIDSDAGER_PER_ÅR).roundToInt()

    private fun tilDagligDouble() = årlig / ARBEIDSDAGER_PER_ÅR

    private fun tilMånedligDouble() = årlig / 12

    private fun rundTilDaglig() = Inntekt((årlig / ARBEIDSDAGER_PER_ÅR).roundToInt() * ARBEIDSDAGER_PER_ÅR.toDouble())

    internal operator fun plus(other: Inntekt) = Inntekt(this.årlig + other.årlig)

    internal operator fun unaryPlus() = this

    internal operator fun unaryMinus() = Inntekt(-årlig)

    internal operator fun minus(other: Inntekt) = this + -other

    internal operator fun times(scalar: Number) = Inntekt(this.årlig * scalar.toDouble())

    internal operator fun div(scalar: Number) = this * (1 / scalar.toDouble())

    internal infix fun ratio(other: Inntekt) = this.årlig / other.årlig

    override fun equals(other: Any?) = other is Inntekt && this.equals(other)

    private fun equals(other: Inntekt) = (this.årlig - other.årlig).absoluteValue < EPSILON

    override fun hashCode() = (årlig / EPSILON).roundToLong().hashCode()

    override fun compareTo(other: Inntekt) = if (this == other) 0 else this.årlig.compareTo(other.årlig)

    override fun toString(): String = "[Årlig: $årlig, Månedlig: ${tilMånedligDouble()}, Daglig: ${tilDagligDouble()}]"
}

internal operator fun Number.times(other: Inntekt) = other * this
