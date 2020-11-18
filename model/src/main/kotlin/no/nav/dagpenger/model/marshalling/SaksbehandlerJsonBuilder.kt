package no.nav.dagpenger.model.marshalling

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import no.nav.dagpenger.model.factory.FaktaRegel
import no.nav.dagpenger.model.faktum.Dokument
import no.nav.dagpenger.model.faktum.Faktum
import no.nav.dagpenger.model.faktum.GrunnleggendeFaktum
import no.nav.dagpenger.model.faktum.Inntekt
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.faktum.UtledetFaktum
import no.nav.dagpenger.model.faktum.ValgFaktum
import no.nav.dagpenger.model.regel.Regel
import no.nav.dagpenger.model.seksjon.Seksjon
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.model.subsumsjon.EnkelSubsumsjon
import no.nav.dagpenger.model.subsumsjon.GodkjenningsSubsumsjon
import no.nav.dagpenger.model.visitor.SøknadprosessVisitor
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

class SaksbehandlerJsonBuilder(
    søknadprosess: Søknadprosess,
    private val seksjonNavn: String,
    private val indeks: Int = 0
) : SøknadprosessVisitor {
    private val mapper = ObjectMapper()
    private val root: ObjectNode = mapper.createObjectNode()
    private val faktaNode = mapper.createArrayNode()
    private val subsumsjonRoot = mapper.createArrayNode()
    private var ignore = true
    private var iValg = false
    private val faktumIder = mutableSetOf<String>()
    private val subsumsjonNoder = mutableListOf<ObjectNode>()

    init {
        søknadprosess.søknad.accept(this)
        søknadprosess.first { seksjonNavn == it.navn && indeks == it.indeks }.filtrertSeksjon(søknadprosess.rootSubsumsjon).accept(this)
        søknadprosess.rootSubsumsjon.mulige().accept(this)
    }

    fun resultat() = root

    override fun preVisit(søknad: Søknad, fnr: String, versjonId: Int, uuid: UUID) {
        root.put("@event_name", "oppgave")
        root.put("@opprettet", "${LocalDateTime.now()}")
        root.put("@id", "${UUID.randomUUID()}")
        root.put("fnr", fnr)
        root.put("soknad_uuid", "$uuid")
        root.put("seksjon_navn", seksjonNavn)
        root.put("indeks", indeks)
        root.set("fakta", faktaNode)
        root.set("subsumsjoner", subsumsjonRoot)
    }

    override fun preVisit(seksjon: Seksjon, rolle: Rolle, fakta: Set<Faktum<*>>, indeks: Int) {
        ignore = false
    }

    override fun postVisit(seksjon: Seksjon, rolle: Rolle, indeks: Int) {
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
        if (iValg) return
        lagFaktumNode<R>(id, roller)
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
        lagFaktumNode(id, roller, svar)
    }

    override fun <R : Comparable<R>> preVisit(
        faktum: UtledetFaktum<R>,
        id: String,
        avhengigeFakta: Set<Faktum<*>>,
        avhengerAvFakta: Set<Faktum<*>>,
        children: Set<Faktum<*>>,
        clazz: Class<R>,
        regel: FaktaRegel<R>
    ) {
        lagFaktumNode<R>(id)
    }

    override fun <R : Comparable<R>> preVisit(
        faktum: UtledetFaktum<R>,
        id: String,
        avhengigeFakta: Set<Faktum<*>>,
        avhengerAvFakta: Set<Faktum<*>>,
        children: Set<Faktum<*>>,
        clazz: Class<R>,
        regel: FaktaRegel<R>,
        svar: R
    ) {
        lagFaktumNode(id, svar = svar)
    }

    override fun preVisit(
        faktum: ValgFaktum,
        id: String,
        avhengigeFakta: Set<Faktum<*>>,
        avhengerAvFakta: Set<Faktum<*>>,
        underordnedeJa: Set<Faktum<Boolean>>,
        underordnedeNei: Set<Faktum<Boolean>>,
        clazz: Class<Boolean>
    ) {
        lagFaktumNode<Boolean>(id)
        iValg = true
    }

    override fun preVisit(
        faktum: ValgFaktum,
        id: String,
        avhengigeFakta: Set<Faktum<*>>,
        avhengerAvFakta: Set<Faktum<*>>,
        underordnedeJa: Set<Faktum<Boolean>>,
        underordnedeNei: Set<Faktum<Boolean>>,
        clazz: Class<Boolean>,
        svar: Boolean
    ) {
        lagFaktumNode(id, svar = svar)
        iValg = true
    }

    override fun postVisit(
        faktum: ValgFaktum,
        id: String,
        underordnedeJa: Set<Faktum<Boolean>>,
        underordnedeNei: Set<Faktum<Boolean>>,
        clazz: Class<Boolean>
    ) {
        iValg = false
    }

    override fun preVisit(subsumsjon: GodkjenningsSubsumsjon, resultat: Boolean?) {
        subsumsjonNoder.add(0, subsumsjonRoot.addObject())
    }

    override fun postVisit(subsumsjon: GodkjenningsSubsumsjon, resultat: Boolean?) {
        subsumsjonNoder.removeAt(0)
    }

    override fun preVisit(subsumsjon: EnkelSubsumsjon, regel: Regel, fakta: List<Faktum<*>>, lokaltResultat: Boolean?, resultat: Boolean?) {
        subsumsjonRoot.addObject().also { subsumsjonNode ->
            subsumsjonNoder.add(0, subsumsjonNode)
            subsumsjonNode.put("resultat", resultat)
            subsumsjonNode.put("lokalt_resultat", lokaltResultat)
            subsumsjonNode.put("navn", subsumsjon.navn)
            subsumsjonNode.put("type", "Enkel subsumsjon")
        }
    }

    override fun postVisit(subsumsjon: EnkelSubsumsjon, regel: Regel, fakta: List<Faktum<*>>, lokaltResultat: Boolean?, resultat: Boolean?) {
        subsumsjonNoder.removeAt(0)
    }

    private fun <R : Comparable<R>> lagFaktumNode(id: String, roller: Set<Rolle> = emptySet(), svar: R? = null) {
        if (ignore) return
        if (id in faktumIder) return
        faktaNode.addObject().also { faktumNode ->
            faktumNode.put("id", id)
            faktumNode.set("roller", mapper.valueToTree(roller.map { it.typeNavn }))
            svar?.also { faktumNode.putR(it) }
        }
        faktumIder.add(id)
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
