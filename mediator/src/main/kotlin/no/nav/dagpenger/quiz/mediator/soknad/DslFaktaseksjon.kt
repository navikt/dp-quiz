package no.nav.dagpenger.quiz.mediator.soknad

import no.nav.dagpenger.model.factory.FaktumFactory
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.seksjon.Seksjon
import no.nav.dagpenger.model.subsumsjon.DeltreSubsumsjon
import kotlin.reflect.full.createType
import kotlin.reflect.full.declaredMemberProperties

interface DslFaktaseksjon {
    val fakta: List<FaktumFactory<*>>

    fun fakta(): Array<FaktumFactory<*>> = fakta.toTypedArray()

    fun databaseIder(): IntArray = this::class.declaredMemberProperties
        .filter { felter ->
            felter.returnType == Int::class.createType()
        }
        .map { heltallsfelter ->
            heltallsfelter.call() as Int
        }
        .toIntArray()

    fun seksjon(søknad: Søknad): List<Seksjon>
    fun regeltre(søknad: Søknad): DeltreSubsumsjon
}
