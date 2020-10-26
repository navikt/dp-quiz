package db

import DataSourceBuilder.dataSource
import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import no.nav.dagpenger.model.fakta.Fakta
import no.nav.dagpenger.model.visitor.FaktaVisitor
import java.util.UUID

// Forstår initialisering av faktum tabellen
class FaktumTable(fakta: Fakta, versjonId: Int) : FaktaVisitor {

    init {
        if (!exists(versjonId)) fakta.accept(this)
    }

    companion object {
        private fun exists(versjonId: Int): Boolean {
            val query = queryOf("SELECT VERSJON_ID FROM FAKTUM WHERE VERSJON_ID = $versjonId")
            return using(sessionOf(dataSource)) { session ->
                session.run(
                    query.map { true }.asSingle
                ) ?: false
            }
        }
    }

    override fun preVisit(fakta: Fakta, fnr: String, versjonId: Int, uuid: UUID) {
        super.preVisit(fakta, fnr, versjonId, uuid)
    }
}
