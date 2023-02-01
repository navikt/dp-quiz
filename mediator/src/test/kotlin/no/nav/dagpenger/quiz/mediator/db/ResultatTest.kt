package no.nav.dagpenger.quiz.mediator.db

import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.boolsk
import no.nav.dagpenger.model.faktum.Fakta
import no.nav.dagpenger.model.faktum.FaktaVersjon
import no.nav.dagpenger.model.faktum.Identer
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.marshalling.ResultatJsonBuilder
import no.nav.dagpenger.model.regel.er
import no.nav.dagpenger.model.seksjon.Seksjon
import no.nav.dagpenger.model.seksjon.Utredningsprosess
import no.nav.dagpenger.model.seksjon.Versjon
import no.nav.dagpenger.quiz.mediator.db.PostgresDataSourceBuilder.dataSource
import no.nav.dagpenger.quiz.mediator.helpers.Postgres
import no.nav.dagpenger.quiz.mediator.helpers.Testprosess
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class ResultatTest {
    companion object {
        internal val IDENT = Identer.Builder().folkeregisterIdent("12020052345").build()
    }

    private lateinit var utredningsprosess: Utredningsprosess
    private lateinit var faktaRecord: FaktaRecord
    private lateinit var resultatRecord: ResultatRecord

    private fun setup(prosessVersjon: FaktaVersjon) {
        val prototypeFakta = Fakta(
            prosessVersjon,
            boolsk faktum "f1" id 19
        )

        Versjon.Bygger(
            prototypeFakta,
            prototypeFakta boolsk 19 er true,
            Utredningsprosess(
                Seksjon(
                    "seksjon",
                    Rolle.nav,
                    *(prototypeFakta.map { it }.toTypedArray())
                )
            ),
        ).registrer()

        Postgres.withMigratedDb {
            FaktumTable(prototypeFakta)
            faktaRecord = FaktaRecord()
            resultatRecord = ResultatRecord()

            utredningsprosess = faktaRecord.ny(
                IDENT,
                prosessVersjon
            )
        }
    }

    @Test
    fun `Lagre resultat`() {
        setup(FaktaVersjon(Testprosess.Test, 935))
        utredningsprosess.boolsk(19).besvar(false)
        val resultat = utredningsprosess.resultat()
        resultatRecord.lagreResultat(
            resultat!!,
            utredningsprosess.fakta.uuid,
            ResultatJsonBuilder(utredningsprosess).resultat()
        )
        val hentaResultat = resultatRecord.hentResultat(utredningsprosess.fakta.uuid)

        assertEquals(resultat, hentaResultat)
    }

    @Test
    fun `Lagrer sendt til manuell behandling`() {
        setup(FaktaVersjon(Testprosess.Test, 936))
        val seksjonsnavn = "manuell seksjon"
        resultatRecord.lagreManuellBehandling(utredningsprosess.fakta.uuid, seksjonsnavn)
        val grunn = using(sessionOf(dataSource)) { session ->
            session.run(
                queryOf( //language=PostgreSQL
                    "SELECT grunn FROM manuell_behandling WHERE soknad_id = (SELECT soknad.id FROM soknad WHERE soknad.uuid = ?)",
                    utredningsprosess.fakta.uuid
                ).map { it.string("grunn") }.asSingle
            )
        }

        assertEquals(seksjonsnavn, grunn)
    }
}
