package db

import DataSourceBuilder.dataSource
import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import no.nav.dagpenger.model.fakta.Fakta
import no.nav.dagpenger.model.søknad.Søknad
import no.nav.dagpenger.model.søknad.Versjon
import no.nav.dagpenger.model.visitor.FaktaVisitor
import java.time.LocalDateTime
import java.util.UUID

// Understands a relational representation of a Fakta
class FaktaRecord : FaktaPersistance {
    override fun ny(fnr: String, søknadType: Versjon.Type): Søknad {
        return Versjon.siste.søknad(fnr, søknadType).also { søknad ->
            NyFakta(søknad.fakta)
        }
    }

    override fun hent(uuid: UUID, søknadType: Versjon.Type): Søknad {
        TODO("Not yet implemented")
    }

    override fun lagre(fakta: Fakta): Boolean {
        TODO("Not yet implemented")
    }

    override fun opprettede(fnr: String): Map<UUID, LocalDateTime> {
        TODO("Not yet implemented")
    }

    private class NyFakta(fakta: Fakta) : FaktaVisitor {
        private var faktaId = 0

        init {
            fakta.accept(this)
        }

        override fun preVisit(fakta: Fakta, fnr: String, versjonId: Int, uuid: UUID) {
            faktaId = using(sessionOf(dataSource)) { session ->
                session.run(
                    queryOf(
                        "INSERT INTO fakta(uuid, versjon_id, fnr) VALUES (?, ?, ?) returning id",
                        uuid,
                        versjonId,
                        fnr
                    ).map { it.int(1) }.asSingle
                )!!
            }
        }
    }
}
