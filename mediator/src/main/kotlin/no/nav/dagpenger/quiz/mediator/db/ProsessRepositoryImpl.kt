package no.nav.dagpenger.quiz.mediator.db

import kotliquery.queryOf
import kotliquery.sessionOf
import no.nav.dagpenger.model.faktum.Faktatype
import no.nav.dagpenger.model.faktum.Identer
import no.nav.dagpenger.model.seksjon.Prosess
import no.nav.dagpenger.model.seksjon.Prosesstype
import no.nav.dagpenger.model.seksjon.Versjon
import java.util.UUID

class ProsessRepositoryImpl : ProsessRepository {
    private val faktaRepository = FaktaRecord()

    override fun ny(person: Identer, prosesstype: Prosesstype, uuid: UUID, faktaUUID: UUID): Prosess {
        val fakta = faktaRepository.ny(person, prosesstype, uuid)
        return Versjon.id(prosesstype).utredningsprosess(fakta)
    }

    override fun hent(uuid: UUID): Prosess {
        val rad = sessionOf(PostgresDataSourceBuilder.dataSource).use { session ->
            session.run(
                queryOf(
                    //language=PostgreSQL
                    """SELECT prosess.person_id, prosess.type, prosess.fakta_id
                    FROM prosess 
                    WHERE uuid = ?
                    """.trimMargin(),
                    uuid,
                ).map { row ->
                    ProsessRad(row.uuid("person_id"), row.string("type"), row.uuid("fakta_id"))
                }.asSingle,
            )
        } ?: throw IllegalArgumentException("Kan ikke hente en utredningsprosess som ikke finnes, uuid: $uuid")
        val utredningsprosess = Versjon.id(rad.prosesstype).utredningsprosess(PersonRecord().hentPerson(rad.personId), uuid, rad.faktaUUID)
        return faktaRepository.rehydrerProsess(utredningsprosess)
    }

    override fun lagre(prosess: Prosess) = faktaRepository.lagre(prosess.fakta)

    private data class ProsessFakta(override val id: String) : Faktatype
    private data class ProsessType(override val faktatype: Faktatype) : Prosesstype
    private data class ProsessRad(val personId: UUID, val type: String, val faktaUUID: UUID) {
        val prosesstype = ProsessType(ProsessFakta(type))
    }
}
