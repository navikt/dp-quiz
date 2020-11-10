package no.nav.dagpenger.model.marshalling

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import no.nav.dagpenger.model.faktagrupper.Faktagrupper
import no.nav.dagpenger.model.faktagrupper.Seksjon
import no.nav.dagpenger.model.faktum.Dokument
import no.nav.dagpenger.model.faktum.Faktum
import no.nav.dagpenger.model.faktum.GrunnleggendeFaktum
import no.nav.dagpenger.model.faktum.Inntekt
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.visitor.FaktagrupperVisitor
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

internal class SaksbehandlerJsonBuilder(
    fakta: Faktagrupper,
    private val seksjonNavn: String
) : FaktagrupperVisitor {
    private val mapper = ObjectMapper()
    private val root: ObjectNode = mapper.createObjectNode()
    private val faktaNode = mapper.createArrayNode()
    private var ignore = true

    init {
        fakta.accept(this)
    }

    internal fun resultat() = root

    override fun preVisit(søknad: Søknad, fnr: String, versjonId: Int, uuid: UUID) {
        root.put("@event_name", "oppgave")
        root.put("@opprettet", "${LocalDateTime.now()}")
        root.put("@id", "${UUID.randomUUID()}")
        root.put("fnr", fnr)
        root.put("uuid", "$uuid")
        root.put("seksjon_navn", seksjonNavn)
        root.set("fakta", faktaNode)
    }

    override fun preVisit(seksjon: Seksjon, rolle: Rolle, fakta: Set<Faktum<*>>) {
        if (seksjonNavn != seksjon.navn) return
        ignore = false
    }

    override fun postVisit(seksjon: Seksjon, rolle: Rolle) {
        ignore = true
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
        faktaNode.addObject().also { faktumNode ->
            faktumNode.put("id", id)
            faktumNode.set("roller", mapper.valueToTree(roller.map { it.name }))
        }
    }

    override fun <R : Comparable<R>> visit(
        faktum: GrunnleggendeFaktum<R>,
        tilstand: Faktum.FaktumTilstand,
        id: String,
        avhengigeFakta: Set<Faktum<*>>,
        avhengerAvFakta: Set<Faktum<*>>,
        roller: Set<Rolle>,
        clazz: Class<R>,
        svar: R
    ) {
        if (ignore) return
        faktaNode.addObject().also { faktumNode ->
            faktumNode.put("id", id)
            faktumNode.set("roller", mapper.valueToTree(roller.map { it.name }))
            faktumNode.putR(svar)
        }
    }

    private fun <R : Comparable<R>> ObjectNode.putR(svar: R) {
        when (svar) {
            is Boolean -> this.put("svar", svar)
            is Int -> this.put("svar", svar)
            is Double -> this.put("svar", svar)
            is String -> this.put("svar", svar)
            is LocalDate -> this.put("svar", svar.toString())
            is Dokument -> this.set(
                "svar",
                svar.reflection { lastOppTidsstempel, url ->
                    mapper.createObjectNode().also {
                        it.put("lastOppTidsstempel", lastOppTidsstempel.toString())
                        it.put("url", url)
                    }
                }
            )
            is Inntekt -> this.put("svar", svar.reflection { årlig, _, _, _ -> årlig })
            else -> throw IllegalArgumentException("Ukjent datatype")
        }
    }
}