package no.nav.dagpenger.quiz.mediator.db

import kotliquery.queryOf
import kotliquery.sessionOf
import no.nav.dagpenger.model.faktum.Faktatype
import no.nav.dagpenger.model.faktum.Faktaversjon
import no.nav.dagpenger.model.faktum.Identer
import no.nav.dagpenger.model.seksjon.Prosess
import no.nav.dagpenger.model.seksjon.Versjon
import java.util.UUID

class ProsessRepositoryImpl : ProsessRepository {
    private val faktaRepository = FaktaRecord()
    override fun ny(person: Identer, faktaversjon: Faktaversjon, uuid: UUID): Prosess {
        val fakta = faktaRepository.ny(person, faktaversjon, uuid)
        return Versjon.id(faktaversjon).utredningsprosess(fakta)
    }

    override fun hent(uuid: UUID): Prosess {
        val rad = sessionOf(PostgresDataSourceBuilder.dataSource).use { session ->
            session.run(
                queryOf(
                    //language=PostgreSQL
                    """SELECT soknad.person_id, versjon.navn, versjon.versjon_id
                    FROM soknad
                             JOIN v1_prosessversjon AS versjon ON (versjon.id = soknad.versjon_id)
                    WHERE uuid = ?
                    """.trimMargin(),
                    uuid,
                ).map { row ->
                    ProsessRad(row.string(1), row.string(2), row.int(3))
                }.asSingle,
            )
        } ?: throw IllegalArgumentException("Kan ikke hente en utredningsprosess som ikke finnes, uuid: $uuid")
        val versjonId = Faktaversjon(ProsessFakta(rad.navn), rad.versjonId)
        val utredningsprosess = Versjon.id(versjonId).utredningsprosess(PersonRecord().hentPerson(rad.personId), uuid)
        return faktaRepository.rehydrerProsess(utredningsprosess)
    }

    override fun lagre(prosess: Prosess) = faktaRepository.lagre(prosess.fakta)

    private data class ProsessFakta(override val id: String) : Faktatype
    private data class ProsessRad(val personId: UUID, val navn: String, val versjonId: Int) {
        constructor(personId: String, navn: String, versjonId: Int) : this(UUID.fromString(personId), navn, versjonId)
    }
}
