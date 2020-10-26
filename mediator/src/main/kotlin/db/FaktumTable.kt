package db

import DataSourceBuilder.dataSource
import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import no.nav.dagpenger.model.fakta.Fakta
import no.nav.dagpenger.model.fakta.Faktum
import no.nav.dagpenger.model.fakta.FaktumId
import no.nav.dagpenger.model.fakta.GrunnleggendeFaktum
import no.nav.dagpenger.model.fakta.Rolle
import no.nav.dagpenger.model.fakta.UtledetFaktum
import no.nav.dagpenger.model.visitor.FaktaVisitor

// Forstår initialisering av faktum tabellen
class FaktumTable(fakta: Fakta, private val versjonId: Int) : FaktaVisitor {

    private var rootId: Int = 0
    private var indeks: Int = 0
    private var skipFaktum = false

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

    override fun visit(faktumId: FaktumId, rootId: Int, indeks: Int) {
        this.rootId = rootId
        this.indeks = indeks
    }

    override fun <R : Comparable<R>> visit(faktum: GrunnleggendeFaktum<R>, tilstand: Faktum.FaktumTilstand, id: String, avhengigeFakta: Set<Faktum<*>>, roller: Set<Rolle>, clazz: Class<R>) {
        if (skipFaktum) return
        using(sessionOf(dataSource)) { session ->
            session.run(
                queryOf(
                    """WITH inserted_id as (INSERT INTO navn (navn) values (?) returning id)
                               INSERT INTO faktum (versjon_id, faktum_type, root_id, indeks, navn_id) SELECT ?, ?, ?, ?, id from inserted_id""".trimMargin(),
                    faktum.navn,
                    versjonId,
                    1,
                    rootId,
                    indeks
                ).asExecute
            )
        }
    }

    override fun <R : Comparable<R>> preVisit(faktum: UtledetFaktum<R>, id: String, avhengigeFakta: Set<Faktum<*>>, children: Set<Faktum<*>>, clazz: Class<R>) {
        skipFaktum = true
    }

    override fun <R : Comparable<R>> postVisit(faktum: UtledetFaktum<R>, id: String, children: Set<Faktum<*>>, clazz: Class<R>) {
        skipFaktum = false
    }
}
