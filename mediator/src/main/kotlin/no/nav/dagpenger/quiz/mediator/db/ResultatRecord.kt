package no.nav.dagpenger.quiz.mediator.db

import PostgresDataSourceBuilder.dataSource
import com.fasterxml.jackson.databind.node.ObjectNode
import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import org.postgresql.util.PGobject
import java.util.UUID

interface ResultatPersistence {
    fun lagreResultat(resultat: Boolean, søknadUuid: UUID, resultatJson: ObjectNode)
    fun hentResultat(uuid: UUID): Boolean
    fun lagreManuellBehandling(uuid: UUID, grunn: String)
}

// Skjønner utfallet av behandlingen av en søknad
class ResultatRecord : ResultatPersistence {

    override fun lagreResultat(resultat: Boolean, søknadUuid: UUID, resultatJson: ObjectNode) {
        using(sessionOf(dataSource)) { session ->
            session.run(
                queryOf( //language=PostgreSQL
                    """
                    INSERT INTO resultat (resultat, data, soknad_id) 
                        SELECT ?, ?, soknad.id
                        FROM soknad 
                        WHERE soknad.uuid = ? 
                    """.trimMargin(),
                    resultat,
                    PGobject().apply {
                        type = "jsonb"
                        value = resultatJson.toString()
                    },
                    søknadUuid
                ).asExecute
            )
        }
    }

    override fun hentResultat(uuid: UUID): Boolean {
        return using(sessionOf(dataSource)) { session ->
            session.run(
                queryOf( //language=PostgreSQL
                    "SELECT resultat FROM resultat WHERE soknad_id = (SELECT soknad.id FROM soknad WHERE soknad.uuid = ?)",
                    uuid
                ).map { it.boolean("resultat") }.asSingle
            )
        } ?: throw IllegalArgumentException("Resultat finnes ikke for søknad, uuid: $uuid")
    }

    override fun lagreManuellBehandling(søknadUuid: UUID, grunn: String) {
        using(sessionOf(dataSource)) { session ->
            session.run(
                queryOf( //language=PostgreSQL
                    """
                    INSERT INTO manuell_behandling (soknad_id, grunn) 
                        SELECT soknad.id, ?
                        FROM soknad 
                        WHERE soknad.uuid = ? 
                    """.trimMargin(),
                    grunn,
                    søknadUuid
                ).asExecute
            )
        }
    }
}
