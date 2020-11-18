package no.nav.dagpenger.model.marshalling

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import no.nav.dagpenger.model.faktum.Dokument
import no.nav.dagpenger.model.faktum.Faktum
import no.nav.dagpenger.model.faktum.FaktumId
import no.nav.dagpenger.model.faktum.GrunnleggendeFaktum
import no.nav.dagpenger.model.faktum.Inntekt
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.seksjon.Seksjon
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.model.visitor.SøknadprosessVisitor
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

class NavJsonBuilder(søknadprosess: Søknadprosess, seksjonNavn: String, indeks: Int = 0) : SøknadprosessVisitor {
    private val mapper = ObjectMapper()
    private val root: ObjectNode = mapper.createObjectNode()
    private val faktaNode = mapper.createArrayNode()
    private val faktumIder = mutableSetOf<String>()
    private val behovNode = mapper.createArrayNode()
    private lateinit var faktumNavBehov: FaktumNavBehov
    private var ignore = true
    private var rootId = 0

    init {
        søknadprosess.søknad.accept(this)
        søknadprosess.first { seksjonNavn == it.navn && indeks == it.indeks }.filtrertSeksjon(søknadprosess.rootSubsumsjon).accept(this)
    }

    fun resultat() = root

    override fun preVisit(søknad: Søknad, fnr: String, versjonId: Int, uuid: UUID) {
        root.put("@event_name", "behov")
        root.put("@opprettet", "${LocalDateTime.now()}")
        root.put("@id", "${UUID.randomUUID()}")
        root.put("fnr", fnr)
        root.put("søknad_uuid", "$uuid")
        root.set("fakta", faktaNode)
        root.set("@behov", behovNode)

        faktumNavBehov = FaktumNavBehov.id(versjonId)
    }

    override fun preVisit(seksjon: Seksjon, rolle: Rolle, fakta: Set<Faktum<*>>, indeks: Int) {
        ignore = false
    }

    override fun postVisit(seksjon: Seksjon, rolle: Rolle, indeks: Int) {
        ignore = true
    }

    override fun visit(faktumId: FaktumId, rootId: Int, indeks: Int) {
        this.rootId = rootId
    }

    override fun <R : Comparable<R>> visit(
        faktum: GrunnleggendeFaktum<R>,
        tilstand: Faktum.FaktumTilstand,
        id: String,
        avhengigeFakta: Set<Faktum<*>>,
        avhengerAvFakta: Set<Faktum<*>>,
        roller: Set<Rolle>,
        clazz: Class<R>
    ) {
        if (ignore) return
        if (id in faktumIder) return
        if (avhengerAvFakta.all { it.erBesvart() }) {
            behovNode.add(faktumNavBehov[rootId])
            lagFaktumNode(id)
            avhengerAvFakta.forEach {
                root.putR(faktumNavBehov[it.reflection { rootId, _ -> rootId }], it.svar())
            }
        }
    }

    private fun lagFaktumNode(id: String) {
        if (ignore) return
        if (id in faktumIder) return
        faktaNode.addObject().also { faktumNode ->
            faktumNode.put("id", id)
            faktumNode.put("behov", faktumNavBehov[rootId])
        }
        faktumIder.add(id)
    }

    private fun ObjectNode.putR(key: String, svar: Any) {
        when (svar) {
            is Boolean -> this.put(key, svar)
            is Int -> this.put(key, svar)
            is Double -> this.put(key, svar)
            is String -> this.put(key, svar)
            is LocalDate -> this.put(key, svar.toString())
            is Dokument -> this.set(
                key,
                svar.reflection { lastOppTidsstempel, url ->
                    mapper.createObjectNode().also {
                        it.put("lastOppTidsstempel", lastOppTidsstempel.toString())
                        it.put("url", url)
                    }
                }
            )
            is Inntekt -> this.put(key, svar.reflection { årlig, _, _, _ -> årlig })
            else -> throw IllegalArgumentException("Ukjent datatype")
        }
    }
}
