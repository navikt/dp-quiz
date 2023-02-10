package no.nav.dagpenger.quiz.mediator.db

import kotliquery.queryOf
import kotliquery.sessionOf
import no.nav.dagpenger.model.faktum.Faktatype
import no.nav.dagpenger.model.faktum.Faktaversjon
import no.nav.dagpenger.model.faktum.Identer
import no.nav.dagpenger.model.seksjon.Utredningsprosess
import no.nav.dagpenger.model.seksjon.Versjon
import java.util.UUID

class UtredningsprosessRepositoryImpl : UtredningsprosessRepository {
    private val faktaRepository = FaktaRecord()
    override fun ny(person: Identer, faktaversjon: Faktaversjon, uuid: UUID): Utredningsprosess {
        val fakta = faktaRepository.ny(person, faktaversjon, uuid)
        return Versjon.id(faktaversjon).utredningsprosess(fakta)
    }

    override fun hent(uuid: UUID): Utredningsprosess {
        val rad = sessionOf(PostgresDataSourceBuilder.dataSource).use { session ->
            session.run(
                queryOf(
                    //language=PostgreSQL
                    "SELECT versjon.navn , versjon.versjon_id FROM soknad JOIN v1_prosessversjon AS versjon ON (versjon.id = soknad.versjon_id) WHERE uuid = ?",
                    uuid,
                ).map { row ->
                    ProsessRad(row.string(1), row.int(2))
                }.asSingle,
            )
        } ?: throw IllegalArgumentException("Kan ikke hente en søknad som ikke finnes, uuid: $uuid")
        val fakta = faktaRepository.hent(uuid)
        return Versjon.id(Faktaversjon(Prosess(rad.navn), rad.versjonId)).utredningsprosess(fakta)
    }

    override fun lagre(utredningsprosess: Utredningsprosess) = faktaRepository.lagre(utredningsprosess.fakta)

    private data class Prosess(override val id: String) : Faktatype
    private data class ProsessRad(val navn: String, val versjonId: Int)
}
