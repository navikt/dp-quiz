package no.nav.dagpenger.quiz.mediator.integration.dummy

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.boolsk
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dato
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.desimaltall
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.envalg
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.flervalg
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.heltall
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.land
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.periode
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.tekst
import no.nav.dagpenger.quiz.mediator.soknad.DslFaktaseksjon

// @todo: Vurdere om denne skal inkluderes i SøknadEksempel1.kt i stedet
object DummySeksjon : DslFaktaseksjon {

    const val `dummy boolean` = 1
    const val `dummy valg` = 2
    const val `dummy subfaktum tekst` = 3
    const val `dummy flervalg` = 4
    const val `dummy dropdown` = 5
    const val `dummy int` = 6
    const val `dummy double` = 7
    const val `dummy tekst` = 8
    const val `dummy localdate` = 9
    const val `dummy periode` = 10
    const val `dummy generator` = 11
    const val `generator dummy boolean` = 12
    const val `generator dummy valg` = 13
    const val `generator dummy flervalg` = 14
    const val `generator dummy dropdown` = 15
    const val `generator dummy int` = 16
    const val `generator dummy double` = 17
    const val `generator dummy tekst` = 18
    const val `generator dummy localdate` = 19
    const val `generator dummy periode` = 20
    const val `generator dummy subfaktum tekst` = 21
    const val `dummy land` = 22

    override val fakta = listOf(
        boolsk faktum "faktum.dummy-boolean" id `dummy boolean`,
        envalg faktum "faktum.dummy-valg"
            med "svar.ja"
            med "svar.nei"
            med "svar.vetikke" id `dummy valg`,
        tekst faktum "faktum.dummy-subfaktum-tekst" id `dummy subfaktum tekst`,
        flervalg faktum "faktum.dummy-flervalg"
            med "svar.1"
            med "svar.2"
            med "svar.3" id `dummy flervalg`,
        envalg faktum "faktum.dummy-dropdown"
            med "svar.1"
            med "svar.2"
            med "svar.3" id `dummy dropdown`,
        heltall faktum "faktum.dummy-int" id `dummy int`,
        desimaltall faktum "faktum.dummy-double" id `dummy double`,
        tekst faktum "faktum.dummy-tekst" id `dummy tekst`,
        dato faktum "faktum.dummy-localdate" id `dummy localdate`,
        periode faktum "faktum.dummy-periode" id `dummy periode`,
        tekst faktum "faktum.generator-dummy-subfaktum-tekst" id `generator dummy subfaktum tekst`,
        heltall faktum "faktum.dummy-generator" id `dummy generator`
            genererer `generator dummy boolean`
            og `generator dummy valg`
            og `generator dummy flervalg`
            og `generator dummy dropdown`
            og `generator dummy int`
            og `generator dummy double`
            og `generator dummy tekst`
            og `generator dummy localdate`
            og `generator dummy periode`,
        boolsk faktum "faktum.generator-dummy-boolean" id `generator dummy boolean`,
        envalg faktum "faktum.generator-dummy-valg"
            med "svar.ja"
            med "svar.nei"
            med "svar.vetikke" id `generator dummy valg`,
        flervalg faktum "faktum.generator-dummy-flervalg"
            med "svar.1"
            med "svar.2"
            med "svar.3" id `generator dummy flervalg`,
        envalg faktum "faktum.generator-dummy-dropdown"
            med "svar.1"
            med "svar.2"
            med "svar.3" id `generator dummy dropdown`,
        heltall faktum "faktum.generator-dummy-int" id `generator dummy int`,
        desimaltall faktum "faktum.generator-dummy-double" id `generator dummy double`,
        tekst faktum "faktum.generator-dummy-tekst" id `generator dummy tekst`,
        dato faktum "faktum.generator-dummy-localdate" id `generator dummy localdate`,
        periode faktum "faktum.generator-dummy-periode" id `generator dummy periode`,
        land faktum "faktum.dummy-land" id `dummy land`
    )
}
