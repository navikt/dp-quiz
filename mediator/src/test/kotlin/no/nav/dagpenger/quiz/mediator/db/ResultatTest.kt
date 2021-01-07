package no.nav.dagpenger.quiz.mediator.db

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.boolsk
import no.nav.dagpenger.model.faktum.Identer
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.regel.er
import no.nav.dagpenger.model.seksjon.Seksjon
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.model.seksjon.Versjon
import no.nav.dagpenger.quiz.mediator.helpers.Postgres
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class ResultatTest {
    companion object {
        internal val IDENT = Identer.Builder().folkeregisterIdent("12020052345").build()
    }

    private lateinit var søknadprosess: Søknadprosess
    private lateinit var søknadRecord: SøknadRecord

    @Test
    fun `happy path`() {
        val versjonId = 934
        Postgres.withMigratedDb {
            val prototypeFakta = Søknad(
                versjonId,
                boolsk faktum "f1" id 19
            )
            Versjon.Bygger(
                prototypeFakta,
                prototypeFakta boolsk 19 er true,
                mapOf(
                    Versjon.UserInterfaceType.Web to Søknadprosess(
                        Seksjon(
                            "seksjon",
                            Rolle.nav,
                            *(prototypeFakta.map { it }.toTypedArray())
                        )
                    )
                )
            ).registrer()
            FaktumTable(prototypeFakta, versjonId)
            søknadRecord = SøknadRecord()
            søknadprosess = søknadRecord.ny(
                IDENT,
                Versjon.UserInterfaceType.Web,
                versjonId
            )

            søknadprosess.boolsk(19).besvar(false)
            val resultat = søknadprosess.resultat()
            søknadRecord.lagreResultat(resultat!!, søknadprosess.søknad)
            val hentaResultat = søknadRecord.hentResultat(søknadprosess.søknad.uuid)
            assertEquals(resultat, hentaResultat)
        }
    }
}
