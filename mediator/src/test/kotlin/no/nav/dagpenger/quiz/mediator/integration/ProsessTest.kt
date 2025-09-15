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
import no.nav.dagpenger.quiz.mediator.db.FaktaRecord
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
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class ProsessTest {
    private lateinit var søknadsprosess: Prosess
    private val prosessUUID = UUID.randomUUID()
    private val faktaUUID = UUID.randomUUID()
    private val repository = ProsessRepositoryPostgres()

    @Test
    fun `Besvarer en prosess uten mocks`() =
        medProsess {
            medSeksjon(Bosted) {
                it.land(`hvilket land bor du i`).besvar(Land("NOR"))
            }
            medSeksjon(DinSituasjon) {
                with(it.aktivSeksjon) {
                    assertEquals("din-situasjon", navn)
                    assertEquals(62, antallSpørsmål)
                }

                it
                    .envalg(`mottatt dagpenger siste 12 mnd`)
                    .besvar(Envalg("faktum.mottatt-dagpenger-siste-12-mnd.svar.nei"))
                it.dato(`dagpenger søknadsdato`).besvar(LocalDate.now())
                it.envalg(`type arbeidstid`).besvar(Envalg("faktum.type-arbeidstid.svar.fast"))
                it.generator(arbeidsforhold).besvar(1)
                // Når generatoren er besvart skal det være lagt til nylig genererte faktum i samme seksjon
                assertEquals(116, it.aktivSeksjon.antallSpørsmål)
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

    @Test
    fun `Ved sletting av en prosess så skal tilhørende fakta beholdes, om er brukt i andre prosesser`() =
        medProsess {
            val faktaRepository = FaktaRecord()
            val ekstraProsessUUID = UUID.randomUUID()
            val annenFaktaUUID = UUID.randomUUID()

            medSeksjon(Bosted) {
                it.tekst(`reist tilbake årsak`).besvar(Tekst("Dummy årsak"))
            }
            lagNyProsess(ekstraProsessUUID) // Lag en ny prosess som bruker samme fakta
            lagNyProsess(UUID.randomUUID(), annenFaktaUUID) // Lag en ny prosess som bruker andre fakta
            assertTrue("Fakta brukt i prosess skal eksistere") { faktaRepository.eksisterer(faktaUUID) }

            repository.slett(prosessUUID)

            assertTrue("Fakta skal fortsatt eksistere, fordi den er brukt i andre prosesser") {
                faktaRepository.eksisterer(
                    faktaUUID,
                )
            }

            repository.slett(ekstraProsessUUID)

            assertFalse("Fakta skal ikke lengre eksistere, fordi ingen prosesser bruker den") {
                faktaRepository.eksisterer(faktaUUID)
            }
            assertTrue("Fakta skal ikke eksistere, fordi den har ingenting med prosessen som ble slettet") {
                faktaRepository.eksisterer(annenFaktaUUID)
            }
        }

    private fun medProsess(block: () -> Unit) =
        Postgres.withMigratedDb {
            Dagpenger.registrer { prosess ->
                FaktumTable(prosess.fakta)
            }

            lagNyProsess(prosessUUID)
            block()
        }

    private fun lagNyProsess(
        prosessUUID: UUID = this.prosessUUID,
        faktaUUID: UUID = this.faktaUUID,
    ) {
        søknadsprosess =
            repository.ny(
                Identer(identer = setOf(Identer.Ident(Identer.Ident.Type.FOLKEREGISTERIDENT, "12312312311", false))),
                Prosesser.Søknad,
                prosessUUID,
                faktaUUID,
            )
    }

    private fun lagreOgHent() {
        repository.lagre(søknadsprosess)
        søknadsprosess = repository.hent(prosessUUID)
    }

    private val Prosess.aktivSeksjon
        get() =
            with(this.nesteSeksjoner()[0]) {
                val aktivSeksjon = this@with
                object {
                    val navn = aktivSeksjon.navn
                    val antallSpørsmål = aktivSeksjon.size
                }
            }

    private infix fun Int.index(generatorIndex: Int) = "$this.$generatorIndex"

    private fun <T : DslFaktaseksjon> medSeksjon(
        faktaseksjon: T,
        block: T.(fakta: Prosess) -> Unit,
    ) {
        assertNotEquals(søknadsprosess.nesteSeksjoner().size, 0, "Har vitterligen ikke neste seksjon")
        block(faktaseksjon, søknadsprosess)
        lagreOgHent()
    }
}
