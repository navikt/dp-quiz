package no.nav.dagpenger.quiz.mediator.integration

import no.nav.dagpenger.model.faktum.Envalg
import no.nav.dagpenger.model.faktum.Identer
import no.nav.dagpenger.model.faktum.Land
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Tekst
import no.nav.dagpenger.model.regel.inneholder
import no.nav.dagpenger.model.seksjon.Prosess
import no.nav.dagpenger.model.seksjon.Seksjon
import no.nav.dagpenger.model.subsumsjon.deltre
import no.nav.dagpenger.quiz.mediator.db.FaktumTable
import no.nav.dagpenger.quiz.mediator.db.ProsessRepositoryPostgres
import no.nav.dagpenger.quiz.mediator.helpers.Postgres
import no.nav.dagpenger.quiz.mediator.soknad.DslFaktaseksjon
import no.nav.dagpenger.quiz.mediator.soknad.Prosesser
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.Bosted
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.Dagpenger
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.Dagpenger.prototypeFakta
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.DinSituasjon
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import java.time.LocalDate
import java.util.UUID
import kotlin.test.assertEquals

internal class ProsessTest() {
    private lateinit var søknadsprosess: Prosess
    private val søknadUUID = UUID.randomUUID()
    private val repository = ProsessRepositoryPostgres()

    @Test
    fun `Besvarer en prosess uten mocks`() = medProsess {
        medSeksjon(Bosted) {
            it.land(`hvilket land bor du i`).besvar(Land("NOR"))
        }
        medSeksjon(DinSituasjon) {
            with(it.aktivSeksjon) {
                assertEquals("din-situasjon", navn)
                assertEquals(64, antallSpørsmål)
            }

            it.envalg(`mottatt dagpenger siste 12 mnd`)
                .besvar(Envalg("faktum.mottatt-dagpenger-siste-12-mnd.svar.nei"))
            it.dato(`dagpenger søknadsdato`).besvar(LocalDate.now())
            it.envalg(`type arbeidstid`).besvar(Envalg("faktum.type-arbeidstid.svar.fast"))
            it.generator(arbeidsforhold).besvar(1)
            // Når generatoren er besvart skal det være lagt til nylig genererte faktum i samme seksjon
            assertEquals(118, it.aktivSeksjon.antallSpørsmål)
            it.tekst(`arbeidsforhold navn bedrift` index 1).besvar(Tekst("Hei"))
        }

        assertDoesNotThrow("Mangler genererte faktum i riktig seksjon") { søknadsprosess.aktivSeksjon }
        medSeksjon(DinSituasjon) {
            it.land(`arbeidsforhold land` index 1).besvar(Land("NOR"))
        }
        // Lag ny prosess med samme fakta
        Prosess(
            Prosesser.AvslagPåAlder,
            Seksjon(
                "test",
                Rolle.søker,
                prototypeFakta.envalg(101),
            ),
        ).also { prosess ->
            Dagpenger.henvendelse.leggTilProsess(
                prosess,
                with(prototypeFakta) {
                    "".deltre {
                        (envalg(DinSituasjon.`mottatt dagpenger siste 12 mnd`) inneholder Envalg("faktum.mottatt-dagpenger-siste-12-mnd.svar.ja"))
                    }
                },
            )
        }

        assertEquals(false, søknadsprosess.erFerdig())
    }

    private fun medProsess(block: () -> Unit) = Postgres.withMigratedDb {
        Dagpenger.registrer { fakta ->
            FaktumTable(fakta)
        }

        lagNyProsess()
        block()
    }

    private fun lagNyProsess() {
        søknadsprosess = repository.ny(
            Identer(identer = setOf(Identer.Ident(Identer.Ident.Type.FOLKEREGISTERIDENT, "12312312311", false))),
            Prosesser.Søknad,
            søknadUUID,
        )
    }

    private fun lagreOgHent() {
        repository.lagre(søknadsprosess)
        søknadsprosess = repository.hent(søknadUUID)
    }

    private val Prosess.aktivSeksjon
        get() = with(this.nesteSeksjoner()[0]) {
            val aktivSeksjon = this@with
            object {
                val navn = aktivSeksjon.navn
                val antallSpørsmål = aktivSeksjon.size
            }
        }

    private infix fun Int.index(generatorIndex: Int) = "$this.$generatorIndex"

    private fun <T : DslFaktaseksjon> medSeksjon(faktaseksjon: T, block: T.(fakta: Prosess) -> Unit) {
        assertNotEquals(søknadsprosess.nesteSeksjoner().size, 0, "Har ikke neste seksjon")
        block(faktaseksjon, søknadsprosess)
        lagreOgHent()
    }
}
