package no.nav.dagpenger.quiz.mediator.soknad

import no.nav.dagpenger.model.factory.FaktumFactory
import no.nav.dagpenger.model.faktum.Fakta
import no.nav.dagpenger.model.seksjon.Seksjon
import no.nav.dagpenger.model.subsumsjon.DeltreSubsumsjon
import kotlin.reflect.full.createType
import kotlin.reflect.full.declaredMemberProperties

interface DslFaktaseksjon {

    val fakta: List<FaktumFactory<*>>
    fun fakta(): Array<FaktumFactory<*>> = fakta.toTypedArray()

    val spørsmålsrekkefølgeForSøker: List<Int>
    fun spørsmålsrekkefølgeForSøker() = spørsmålsrekkefølgeForSøker.toIntArray()

    fun databaseIder(): IntArray = this::class.declaredMemberProperties
        .filter { felter ->
            felter.returnType == Int::class.createType()
        }
        .map { heltallsfelter ->
            heltallsfelter.call() as Int
        }
        .toIntArray()

    fun seksjon(fakta: Fakta): List<Seksjon>
    fun regeltre(fakta: Fakta): DeltreSubsumsjon
}
