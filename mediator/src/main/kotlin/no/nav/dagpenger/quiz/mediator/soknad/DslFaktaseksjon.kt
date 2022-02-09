package no.nav.dagpenger.quiz.mediator.soknad

import no.nav.dagpenger.model.factory.FaktumFactory
import kotlin.reflect.full.createType
import kotlin.reflect.full.declaredMemberProperties

interface DslFaktaseksjon {

    val fakta: List<FaktumFactory<*>>

    fun fakta(): Array<FaktumFactory<*>> = fakta.toTypedArray()

    fun variabler(): IntArray = this::class.declaredMemberProperties
        .filter { felter ->
            felter.returnType == Int::class.createType()
        }
        .map { heltallsfelter ->
            heltallsfelter.call() as Int
        }
        .sorted()
        .toIntArray()
}
