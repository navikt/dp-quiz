package no.nav.dagpenger.quiz.mediator.db

import PostgresDataSourceBuilder.dataSource
import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.boolsk
import no.nav.dagpenger.model.faktum.Identer
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.marshalling.ResultatJsonBuilder
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
    private lateinit var resultatRecord: ResultatRecord

    private fun setup(versjonId: Int) {
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

        Postgres.withMigratedDb {
            FaktumTable(prototypeFakta, versjonId)
            søknadRecord = SøknadRecord()
            resultatRecord = ResultatRecord()

            søknadprosess = søknadRecord.ny(
                IDENT,
                Versjon.UserInterfaceType.Web,
                versjonId
            )
        }
    }

    @Test
    fun `Lagre resultat`() {
        setup(935)
        søknadprosess.boolsk(19).besvar(false)

        val resultat = søknadprosess.resultat()
        resultatRecord.lagreResultat(
            resultat!!,
            søknadprosess.søknad.uuid,
            ResultatJsonBuilder(søknadprosess).resultat()
        )

        val hentaResultat = resultatRecord.hentResultat(søknadprosess.søknad.uuid)

        assertEquals(resultat, hentaResultat)
    }

    @Test
    fun `Lagrer sendt til manuell behandling`() {
        setup(936)
        val seksjonsnavn = "manuell seksjon"
        resultatRecord.lagreManuellBehandling(søknadprosess.søknad.uuid, seksjonsnavn)

        val grunn = using(sessionOf(dataSource)) { session ->
            session.run(
                queryOf( //language=PostgreSQL
                    "SELECT grunn FROM manuell_behandling WHERE soknad_id = (SELECT soknad.id FROM soknad WHERE soknad.uuid = ?)",
                    søknadprosess.søknad.uuid
                ).map { it.string("grunn") }.asSingle
            )
        }

        assertEquals(seksjonsnavn, grunn)
    }
}
