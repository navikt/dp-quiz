package no.nav.dagpenger.quiz.mediator.integration

import no.nav.dagpenger.model.faktum.Envalg
import no.nav.dagpenger.model.faktum.Identer
import no.nav.dagpenger.model.faktum.Land
import no.nav.dagpenger.model.seksjon.Utredningsprosess
import no.nav.dagpenger.quiz.mediator.db.FaktumTable
import no.nav.dagpenger.quiz.mediator.db.UtredningsprosessRepositoryImpl
import no.nav.dagpenger.quiz.mediator.helpers.Postgres
import no.nav.dagpenger.quiz.mediator.soknad.DslFaktaseksjon
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.Bosted
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.Dagpenger
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.DinSituasjon
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate
import kotlin.test.assertEquals

internal class UtredningsprosessTest() {
    private lateinit var søknadsprosess: Utredningsprosess

    @Test
    fun `Besvarer en prosess uten mocks`() {
        Postgres.withMigratedDb {
            Dagpenger.registrer { fakta ->
                FaktumTable(fakta)
            }
            søknadsprosess = UtredningsprosessRepositoryImpl().ny(
                Identer(identer = setOf(Identer.Ident(Identer.Ident.Type.FOLKEREGISTERIDENT, "12312312311", false))),
                Dagpenger.VERSJON_ID,
            )
            medSeksjon(Bosted) {
                it.land(`hvilket land bor du i`).besvar(Land("NOR"))
            }
            medSeksjon(DinSituasjon) {
                it.envalg(`mottatt dagpenger siste 12 mnd`)
                    .besvar(Envalg("faktum.mottatt-dagpenger-siste-12-mnd.svar.nei"))
                it.dato(`dagpenger søknadsdato`).besvar(LocalDate.now())
                it.envalg(`type arbeidstid`).besvar(Envalg("faktum.type-arbeidstid.svar.fast"))
                it.heltall(arbeidsforhold).besvar(1)
            }
            assertEquals(false, søknadsprosess.erFerdig())
        }
    }

    private fun <T : DslFaktaseksjon> medSeksjon(faktaseksjon: T, block: T.(fakta: Utredningsprosess) -> Unit) {
        assertNotEquals(søknadsprosess.nesteSeksjoner().size, 0, "Har ikke neste seksjon")
        assertEquals(faktaseksjon.seksjon(søknadsprosess.fakta)[0].navn, søknadsprosess.nesteSeksjoner()[0].navn)
        block(faktaseksjon, søknadsprosess)
    }
}
