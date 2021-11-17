package no.nav.dagpenger.quiz.mediator.soknad

import no.nav.dagpenger.model.faktum.Dokument
import no.nav.dagpenger.model.faktum.Identer
import no.nav.dagpenger.model.faktum.Person
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.model.seksjon.Versjon
import no.nav.dagpenger.quiz.mediator.soknad.Dagpenger.`Avtjent militærtjeneste minst 3 av siste 6 mnd`
import no.nav.dagpenger.quiz.mediator.soknad.Dagpenger.`Bekreftelse fra relevant fagpersonell`
import no.nav.dagpenger.quiz.mediator.soknad.Dagpenger.`Redusert helse, fysisk eller psykisk`
import no.nav.dagpenger.quiz.mediator.soknad.Dagpenger.`Villig til å ta alle typer arbeid`
import no.nav.dagpenger.quiz.mediator.soknad.Dagpenger.`Villig til å ta arbeid i hele Norge`
import no.nav.dagpenger.quiz.mediator.soknad.Dagpenger.`Villig til å ta ethvert arbeid`
import no.nav.dagpenger.quiz.mediator.soknad.Dagpenger.`Villig til å ta hel og deltidsjobb`
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.UUID

class DagpengerTest {
    private lateinit var dagpenger: Søknadprosess

    @BeforeEach
    fun setup() {
        dagpenger = Versjon.Bygger(
            Dagpenger.søknad,
            Dagpenger.Subsumsjoner.regeltre,
            mapOf(Versjon.UserInterfaceType.Web to Dagpenger.søknadsprosess)
        )
            .søknadprosess(
                Person(UUID.randomUUID(), Identer.Builder().folkeregisterIdent("12345678910").build()),
                Versjon.UserInterfaceType.Web
            )
    }

    @Test
    fun `Reell arbeidssøker `() {
        dagpenger.boolsk(`Villig til å ta hel og deltidsjobb`).besvar(true)
        dagpenger.boolsk(`Villig til å ta arbeid i hele Norge`).besvar(true)
        dagpenger.boolsk(`Villig til å ta alle typer arbeid`).besvar(true)
        dagpenger.boolsk(`Villig til å ta ethvert arbeid`).besvar(true)
        dagpenger.boolsk(`Avtjent militærtjeneste minst 3 av siste 6 mnd`).besvar(true)

        assertTrue(dagpenger.erFerdig())
        assertEquals(true, dagpenger.resultat())
    }
    @Test
    fun `Reell arbeidssøker med redusert helse, fysisk eller psykisk svart Ja `() {
        dagpenger.boolsk(`Villig til å ta hel og deltidsjobb`).besvar(false)
        dagpenger.boolsk(`Villig til å ta arbeid i hele Norge`).besvar(true)
        dagpenger.boolsk(`Villig til å ta alle typer arbeid`).besvar(true)
        dagpenger.boolsk(`Villig til å ta ethvert arbeid`).besvar(true)
        dagpenger.boolsk(`Redusert helse, fysisk eller psykisk`).besvar(true)
        dagpenger.dokument(`Bekreftelse fra relevant fagpersonell`).besvar(
            Dokument(
                lastOppTidsstempel = LocalDateTime.now(),
                url = "https://nav.no/sti/til/dokument.pdf"
            )
        )
        dagpenger.boolsk(`Avtjent militærtjeneste minst 3 av siste 6 mnd`).besvar(true)

        assertTrue(dagpenger.erFerdig())
        assertEquals(true, dagpenger.resultat())
    }
}
