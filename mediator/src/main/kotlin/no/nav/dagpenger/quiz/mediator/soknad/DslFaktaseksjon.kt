package no.nav.dagpenger.quiz.mediator.soknad

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.boolsk
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dokument
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.tekst
import no.nav.dagpenger.model.factory.FaktumFactory
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.regel.er
import no.nav.dagpenger.model.regel.godkjentAv
import no.nav.dagpenger.model.regel.utfylt
import no.nav.dagpenger.model.seksjon.Seksjon
import no.nav.dagpenger.model.subsumsjon.DeltreSubsumsjon
import no.nav.dagpenger.model.subsumsjon.GodkjenningsSubsumsjon
import no.nav.dagpenger.model.subsumsjon.deltre
import no.nav.dagpenger.model.subsumsjon.hvisIkkeOppfylt
import no.nav.dagpenger.model.subsumsjon.hvisOppfylt
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
        .sorted()
        .toIntArray()

    fun seksjon(søknad: Søknad): List<Seksjon>
    fun regeltre(søknad: Søknad): DeltreSubsumsjon
}

fun List<Int>.dokumenteresAv(
    navn: String,
    tilgjengeligId: Int,
    årsakId: Int,
    dokumentId: Int,
    godkjenningId: Int
) = Dokumentasjonskrav(navn, tilgjengeligId, årsakId, dokumentId, godkjenningId, this)

class Dokumentasjonskrav(
    private val navn: String,
    private val tilgjengelig: Int,
    private val årsak: Int,
    private val dokumentId: Int,
    private val godkjenning: Int,
    private val dokumenterer: List<Int>
) {
    val fakta: List<FaktumFactory<*>> = listOf(
        boolsk faktum "dokumentasjon av $navn tilgjengelig" id `tilgjengelig`,
        tekst faktum "årsak til at dokumentasjon av $navn ikke sendes inn" id årsak,
        (dokument faktum "dokumentasjon av $navn" id dokumentId).also {
            dokumenterer.forEach { dok -> it.avhengerAv(dok) }
        },
        boolsk faktum "godkjenning av dokumentasjon for $navn" id godkjenning avhengerAv dokumentId og årsak
    )

    fun regeltre(søknad: Søknad): GodkjenningsSubsumsjon = with(søknad) {
        "dokumentasjonskrav for $navn".deltre {
            boolsk(tilgjengelig) er true hvisOppfylt {
                dokument(dokumentId).utfylt()
            } hvisIkkeOppfylt {
                tekst(årsak).utfylt()
            }
        }.godkjentAv(boolsk(godkjenning))
    }
}
