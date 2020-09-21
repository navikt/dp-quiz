package no.nav.dagpenger.model.visitor

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.ObjectWriter
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import no.nav.dagpenger.model.fakta.Dokument
import no.nav.dagpenger.model.fakta.Faktum
import no.nav.dagpenger.model.fakta.GrunnleggendeFaktum
import no.nav.dagpenger.model.fakta.Rolle
import no.nav.dagpenger.model.fakta.UtledetFaktum
import no.nav.dagpenger.model.søknad.Seksjon
import no.nav.dagpenger.model.søknad.Søknad
import java.time.LocalDate
import java.util.UUID

class SøknadJsonBuilder(private val søknad: Søknad) : SøknadVisitor {
    private val mapper = ObjectMapper()
    private var rootSøknad: ObjectNode = mapper.createObjectNode()
    private val arrayNodes: MutableList<ArrayNode> = mutableListOf(mapper.createArrayNode())
    private val objectNodes: MutableList<ObjectNode> = mutableListOf()
    private val faktaNode = mapper.createArrayNode()
    private val faktumIder = mutableSetOf<Int>()

    init {
        søknad.accept(this)
    }

    fun resultat() = mapper.createObjectNode().also {
        it.set("fakta", faktaNode)
        it.set("root", rootSøknad)
    }

    override fun toString() =
        ObjectMapper().writerWithDefaultPrettyPrinter<ObjectWriter>().writeValueAsString(resultat())

    override fun preVisit(søknad: Søknad, uuid: UUID) {
        mapper.createObjectNode().also { søknadNode ->
            objectNodes.add(0, søknadNode)
            søknadNode.put("uuid", uuid.toString())
        }
    }

    override fun postVisit(søknad: Søknad) {
        objectNodes.removeAt(0).also { søknadNode ->
            rootSøknad = søknadNode
            søknadNode.set("seksjoner", arrayNodes.removeAt(0))
        }
    }

    override fun preVisit(seksjon: Seksjon, fakta: Set<Faktum<*>>) {
        mapper.createObjectNode().also { seksjonNode ->
            arrayNodes.first().add(seksjonNode)
            seksjonNode.set("fakta", mapper.valueToTree(fakta.map { it.id }))
        }
    }

    override fun <R : Comparable<R>> preVisit(faktum: UtledetFaktum<R>, id: Int, avhengigeFakta: List<Faktum<*>>, children: Set<Faktum<*>>, svar: R) {
        if (id in faktumIder) return
        faktumNode(faktum, id, avhengigeFakta).also { faktumNode ->
            faktumNode.set("fakta", mapper.valueToTree(children.map { it.id }))
            faktumNode.putR(svar)
        }
        faktumIder.add(id)
    }

    override fun <R : Comparable<R>> preVisit(faktum: UtledetFaktum<R>, id: Int, avhengigeFakta: List<Faktum<*>>, children: Set<Faktum<*>>) {
        if (id in faktumIder) return
        faktumNode(faktum, id, avhengigeFakta).also { faktumNode ->
            faktumNode.set("fakta", mapper.valueToTree(children.map { it.id }))
        }
        faktumIder.add(id)
    }

    override fun <R : Comparable<R>> visit(
        faktum: GrunnleggendeFaktum<R>,
        tilstand: Faktum.FaktumTilstand,
        id: Int,
        avhengigeFakta: List<Faktum<*>>,
        roller: Set<Rolle>
    ) {
        if (id in faktumIder) return
        faktumNode(faktum, id, avhengigeFakta).also { faktumNode ->
            faktumNode.set("roller", mapper.valueToTree(roller.map { it.name }))
        }
        faktumIder.add(id)
    }

    override fun <R : Comparable<R>> visit(
        faktum: GrunnleggendeFaktum<R>,
        tilstand: Faktum.FaktumTilstand,
        id: Int,
        avhengigeFakta: List<Faktum<*>>,
        roller: Set<Rolle>,
        svar: R
    ) {
        if (id in faktumIder) return
        faktumNode(faktum, id, avhengigeFakta).also { faktumNode ->
            faktumNode.set("roller", mapper.valueToTree(roller.map { it.name }))
            faktumNode.putR(svar)
        }
        faktumIder.add(id)
    }

    private fun <R : Comparable<R>> faktumNode(
        faktum: Faktum<R>,
        id: Int,
        avhengigeFakta: List<Faktum<*>>
    ) =
        mapper.createObjectNode().also { faktumNode ->
            faktumNode.put("navn", faktum.navn.toString())
            faktumNode.put("id", id)
            faktumNode.set("avhengigFakta", mapper.valueToTree(avhengigeFakta.map { it.id }))
            faktaNode.add(faktumNode)
        }
}

private fun <R : Comparable<R>> ObjectNode.putR(svar: R) {
    when {
        svar is Boolean -> this.put("svar", svar)
        svar is Int -> this.put("svar", svar)
        svar is Double -> this.put("svar", svar)
        svar is String -> this.put("svar", svar)
        svar is LocalDate -> this.put("svar", svar.toString())
        svar is Dokument -> this.put("svar", svar.toUrl())
        else -> throw IllegalArgumentException("Ukjent datatype")
    }
}
