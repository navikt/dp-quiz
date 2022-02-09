package no.nav.dagpenger.quiz.mediator.soknad.dagpenger

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.boolsk
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.flervalg
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.heltall
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.tekst
import no.nav.dagpenger.quiz.mediator.soknad.DslFaktaseksjon

object ReellArbeidssoker : DslFaktaseksjon {
    const val `hel deltid` = 1
    const val `kun deltid aarsak` = 2
    const val `kun deltid aarsak antall timer` = 3
    const val `hele norge` = 4
    const val `ikke hele norge` = 5
    const val `alle typer arbeid` = 6
    const val `ikke denne type arbeid` = 7
    const val `ethvert arbeid` = 8

    override val fakta = listOf(
        boolsk faktum "faktum.hel-deltid" id `hel deltid`,
        flervalg faktum "faktum.kun-deltid-aarsak"
            med "svar.redusert-helse"
            med "svar.omsorg-baby"
            med "svar.eneansvar-barn"
            med "svar.omsorg-barn-spesielle-behov"
            med "svar.skift-turnus"
            med "svar.annen-situasjon" id `kun deltid aarsak`,
        heltall faktum "faktum.kun-deltid-aarsak-antall-timer" id `kun deltid aarsak antall timer`,
        boolsk faktum "faktum.hele-norge" id `hele norge`,
        flervalg faktum "faktum.ikke-hele-norge"
            med "svar.redusert-helse"
            med "svar.omsorg-baby"
            med "svar.eneansvar-barn"
            med "svar.omsorg-barn-spesielle-behov"
            med "svar.utenfor-naeromraadet"
            med "svar.annen-situasjon" id `ikke hele norge`,
        boolsk faktum "faktum.alle-typer-arbeid" id `alle typer arbeid`,
        tekst faktum "faktum.ikke-denne-type-arbeid" id `ikke denne type arbeid`,
        boolsk faktum "faktum.ethvert-arbeid" id `ethvert arbeid`
    )

    override val alleVariabler = listOf(
        `hel deltid`,
        `kun deltid aarsak`,
        `kun deltid aarsak antall timer`,
        `hele norge`,
        `ikke hele norge`,
        `alle typer arbeid`,
        `ikke denne type arbeid`,
        `ethvert arbeid`,
    )
}
