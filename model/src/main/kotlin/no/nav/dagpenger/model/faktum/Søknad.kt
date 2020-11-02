package no.nav.dagpenger.model.faktum

import no.nav.dagpenger.model.factory.FaktumFactory
import no.nav.dagpenger.model.factory.UtledetFaktumFactory
import no.nav.dagpenger.model.faktagrupper.Seksjon
import no.nav.dagpenger.model.faktagrupper.Versjon
import no.nav.dagpenger.model.visitor.FaktaVisitor
import java.time.LocalDate
import java.util.UUID

class Søknad private constructor(
    private val fnr: String,
    private val versjonId: Int,
    val uuid: UUID,
    private val faktaMap: MutableMap<FaktumId, Faktum<*>>
) : TypedFaktum, Iterable<Faktum<*>> {

    internal val size get() = faktaMap.size

    internal constructor(fnr: String, versjonId: Int, faktumMap: MutableMap<FaktumId, Faktum<*>>) : this(
        fnr,
        versjonId,
        UUID.randomUUID(),
        faktumMap
    )

    constructor(vararg factories: FaktumFactory<*>) : this("", 0, UUID.randomUUID(), factories.toList())

    constructor(fnr: String, versjonId: Int, uuid: UUID, factories: List<FaktumFactory<*>>) : this(
        fnr,
        versjonId,
        uuid,
        factories.toFaktaMap()
    )

    init {
        this.forEach { if (it is GeneratorFaktum) it.søknad = this }
    }

    companion object {
        fun Søknad.seksjon(navn: String, rolle: Rolle, vararg ider: Int) = Seksjon(
            navn,
            rolle,
            *(this.map { it }.toTypedArray())
        )

        private fun List<FaktumFactory<*>>.toFaktaMap() =
            tilFakta()
                .sjekkIder()
                .tilValg(this)
                .tilTemplate(this)
                .angiAvhengigheter(this)
                .tilUtledet(this)

        private fun List<FaktumFactory<*>>.tilFakta() = this.map { factory ->
            factory.faktum().let { faktum ->
                faktum.faktumId to faktum
            }
        }

        private fun List<Pair<FaktumId, Faktum<*>>>.sjekkIder(): List<Pair<FaktumId, Faktum<*>>> =
            this.also { fakta ->
                fakta.groupingBy { it.first }.eachCount().forEach {
                    require(it.value == 1) { "Faktum med ${it.key} er definert mer en 1 gang" }
                }
            }
        private fun MutableMap<FaktumId, Faktum<*>>.tilTemplate(factories: List<FaktumFactory<*>>): MutableMap<FaktumId, Faktum<*>> =
            this.also { faktumMap ->
                factories.forEach { factory ->
                    factory.tilTemplate(faktumMap)
                }
            }

        private fun List<Pair<FaktumId, Faktum<*>>>.tilValg(factories: List<FaktumFactory<*>>): MutableMap<FaktumId, Faktum<*>> =
            this.toMap().toMutableMap().also { faktumMap ->
                factories.forEach { if (it is UtledetFaktumFactory) it.leggTilBarn(faktumMap) }
            }

        private fun MutableMap<FaktumId, Faktum<*>>.angiAvhengigheter(factories: List<FaktumFactory<*>>): MutableMap<FaktumId, Faktum<*>> = this.also { faktumMap ->
            factories.forEach { factory ->
                factory.avhengerAv(faktumMap)
            }
        }

        private fun MutableMap<FaktumId, Faktum<*>>.tilUtledet(factories: List<FaktumFactory<*>>): MutableMap<FaktumId, Faktum<*>> = this.also { faktumMap ->
            factories.forEach { factory ->
                factory.sammensattAv(faktumMap)
            }
        }
    }

    override infix fun id(rootId: Int) = id(FaktumId(rootId))
    override infix fun id(id: String) = id(FaktumId(id))
    internal infix fun id(faktumId: FaktumId) = faktaMap[faktumId]
        ?: throw IllegalArgumentException("Ukjent faktum $faktumId")

    internal infix fun idOrNull(faktumId: FaktumId) = faktaMap[faktumId]
    infix fun idOrNull(id: String) = idOrNull(FaktumId(id))

    override infix fun dokument(rootId: Int) = dokument(FaktumId(rootId))
    override infix fun dokument(id: String) = dokument(FaktumId(id))
    internal infix fun dokument(faktumId: FaktumId) = id(faktumId) as Faktum<Dokument>

    override fun inntekt(rootId: Int) = inntekt(FaktumId(rootId))
    override fun inntekt(id: String) = inntekt(FaktumId(id))
    internal infix fun inntekt(faktumId: FaktumId) = id(faktumId) as Faktum<Inntekt>

    override infix fun ja(rootId: Int) = ja(FaktumId(rootId))
    override infix fun ja(id: String) = ja(FaktumId(id))
    internal infix fun ja(faktumId: FaktumId) = id(faktumId) as Faktum<Boolean>

    override infix fun dato(rootId: Int) = dato(FaktumId(rootId))
    override infix fun dato(id: String) = dato(FaktumId(id))
    internal infix fun dato(faktumId: FaktumId) = id(faktumId) as Faktum<LocalDate>

    override infix fun heltall(rootId: Int) = heltall(FaktumId(rootId))
    override infix fun heltall(id: String) = heltall(FaktumId(id))
    internal infix fun heltall(faktumId: FaktumId) = id(faktumId) as Faktum<Int>

    override infix fun generator(rootId: Int) = generator(FaktumId(rootId))
    override infix fun generator(id: String) = heltall(FaktumId(id))
    internal infix fun generator(faktumId: FaktumId) = id(faktumId) as GeneratorFaktum

    fun bygg(fnr: String, versjonId: Int, uuid: UUID = UUID.randomUUID()): Søknad {
        val byggetFakta = mutableMapOf<FaktumId, Faktum<*>>()
        val mapOfFakta = faktaMap.map { it.key to it.value.bygg(byggetFakta) }.toMap().toMutableMap()
        return Søknad(fnr, versjonId, uuid, mapOfFakta)
    }

    override fun iterator(): MutableIterator<Faktum<*>> {
        return faktaMap.values.sorted().sortUtledet().iterator()
    }

    private fun List<Faktum<*>>.sortUtledet(): MutableList<Faktum<*>> {
        this.forEachIndexed { indeks, faktum ->
            if (faktum !is UtledetFaktum) return@forEachIndexed
            if (faktum.erDefinert(this, indeks)) return@forEachIndexed
            val nyListe = this.toMutableList().also {
                it.add(it.removeAt(indeks))
            }
            return@sortUtledet nyListe.sortUtledet()
        }
        return this.toMutableList()
    }

    internal fun add(faktum: Faktum<*>) {
        faktaMap[faktum.faktumId] = faktum
    }

    internal fun faktagrupper(type: Versjon.FaktagrupperType) = Versjon.id(versjonId).faktagrupper(this, type)

    fun accept(visitor: FaktaVisitor) {
        visitor.preVisit(this, fnr, versjonId, uuid)
        this.forEach { it.accept(visitor) }
        visitor.postVisit(this, fnr, versjonId, uuid)
    }
}
