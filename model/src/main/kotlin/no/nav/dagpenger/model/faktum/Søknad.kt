package no.nav.dagpenger.model.faktum

import no.nav.dagpenger.model.factory.FaktumFactory
import no.nav.dagpenger.model.seksjon.Seksjon
import no.nav.dagpenger.model.visitor.SøknadVisitor
import java.time.LocalDate
import java.util.UUID

@Suppress("UNCHECKED_CAST")
class Søknad private constructor(
    private val person: Person,
    internal val prosessVersjon: Prosessversjon,
    val uuid: UUID,
    private val faktaMap: MutableMap<FaktumId, Faktum<*>>
) : TypedFaktum, Iterable<Faktum<*>> {

    internal val size get() = faktaMap.size

    constructor(prosessVersjon: Prosessversjon, vararg factories: FaktumFactory<*>) : this(
        Person.prototype,
        prosessVersjon,
        UUID.randomUUID(),
        factories.toList()
    )

    constructor(person: Person, prosessVersjon: Prosessversjon, uuid: UUID, factories: List<FaktumFactory<*>>) : this(
        person,
        prosessVersjon,
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
            *(ider.map { id -> this.id(id) }.toTypedArray())
        )
        private fun List<FaktumFactory<*>>.toFaktaMap() =
            tilFakta()
                .sjekkIder()
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

        private fun List<Pair<FaktumId, Faktum<*>>>.tilTemplate(factories: List<FaktumFactory<*>>): MutableMap<FaktumId, Faktum<*>> =
            this.toMap().toMutableMap().also { faktumMap ->
                factories.forEach { factory ->
                    factory.tilTemplate(faktumMap)
                }
            }

        private fun MutableMap<FaktumId, Faktum<*>>.angiAvhengigheter(factories: List<FaktumFactory<*>>): MutableMap<FaktumId, Faktum<*>> =
            this.also { faktumMap ->
                factories.forEach { factory ->
                    factory.avhengerAv(faktumMap)
                }
            }

        private fun MutableMap<FaktumId, Faktum<*>>.tilUtledet(factories: List<FaktumFactory<*>>): MutableMap<FaktumId, Faktum<*>> =
            this.also { faktumMap ->
                factories.forEach { factory ->
                    factory.sammensattAv(faktumMap)
                }
            }
    }

    override infix fun id(rootId: Int) = id(FaktumId(rootId))
    override infix fun id(id: String) = id(FaktumId(id))
    internal infix fun id(faktumId: FaktumId) = faktaMap[faktumId]
        ?: throw IllegalArgumentException("Ukjent faktum $faktumId")

    infix fun idOrNull(faktumId: FaktumId) = faktaMap[faktumId]
    infix fun idOrNull(id: String) = idOrNull(FaktumId(id))

    override infix fun dokument(rootId: Int) = dokument(FaktumId(rootId))
    override infix fun dokument(id: String) = dokument(FaktumId(id))
    internal infix fun dokument(faktumId: FaktumId) = id(faktumId) as Faktum<Dokument>

    override fun inntekt(rootId: Int) = inntekt(FaktumId(rootId))
    override fun inntekt(id: String) = inntekt(FaktumId(id))
    internal infix fun inntekt(faktumId: FaktumId) = id(faktumId) as Faktum<Inntekt>

    override infix fun boolsk(rootId: Int) = boolsk(FaktumId(rootId))
    override infix fun boolsk(id: String) = boolsk(FaktumId(id))
    internal infix fun boolsk(faktumId: FaktumId) = id(faktumId) as Faktum<Boolean>

    override infix fun dato(rootId: Int) = dato(FaktumId(rootId))
    override infix fun dato(id: String) = dato(FaktumId(id))
    internal infix fun dato(faktumId: FaktumId) = id(faktumId) as Faktum<LocalDate>

    override infix fun heltall(rootId: Int) = heltall(FaktumId(rootId))
    override infix fun heltall(id: String) = heltall(FaktumId(id))
    internal infix fun heltall(faktumId: FaktumId) = id(faktumId) as Faktum<Int>

    override infix fun desimaltall(rootId: Int) = desimaltall(FaktumId(rootId))
    override infix fun desimaltall(id: String) = desimaltall(FaktumId(id))
    private infix fun desimaltall(faktumId: FaktumId) = id(faktumId) as Faktum<Double>

    override infix fun generator(rootId: Int) = generator(FaktumId(rootId))
    override infix fun generator(id: String) = generator(FaktumId(id))
    internal infix fun generator(faktumId: FaktumId) = id(faktumId) as GeneratorFaktum

    override fun envalg(rootId: Int) = envalg(FaktumId(rootId))
    override fun envalg(id: String): Faktum<Envalg> = envalg(FaktumId(id))
    private infix fun envalg(faktumId: FaktumId) = id(faktumId) as Faktum<Envalg>

    override fun flervalg(rootId: Int) = flervalg(FaktumId(rootId))
    override fun flervalg(id: String): Faktum<Flervalg> = flervalg(FaktumId(id))
    private infix fun flervalg(faktumId: FaktumId) = id(faktumId) as Faktum<Flervalg>

    override fun tekst(rootId: Int): Faktum<Tekst> = tekst(FaktumId(rootId))
    override fun tekst(id: String): Faktum<Tekst> = tekst(FaktumId(id))
    private infix fun tekst(faktumId: FaktumId) = id(faktumId) as Faktum<Tekst>

    override fun land(rootId: Int): Faktum<Land> = land(FaktumId(rootId))
    override fun land(id: String): Faktum<Land> = land(FaktumId(id))
    private fun land(faktumId: FaktumId) = id(faktumId) as Faktum<Land>

    override fun periode(rootId: Int): Faktum<Periode> = periode(FaktumId(rootId))
    override fun periode(id: String): Faktum<Periode> = periode(FaktumId(id))
    private infix fun periode(faktumId: FaktumId) = id(faktumId) as Faktum<Periode>

    fun bygg(person: Person, prosessVersjon: Prosessversjon, uuid: UUID = UUID.randomUUID()): Søknad {
        val byggetFakta = mutableMapOf<FaktumId, Faktum<*>>()
        val mapOfFakta = faktaMap.map { it.key to it.value.bygg(byggetFakta) }.toMap().toMutableMap()
        return Søknad(person, prosessVersjon, uuid, mapOfFakta)
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

    internal fun removeAll(templates: List<TemplateFaktum<*>>) {
        faktaMap.keys.toList().forEach { faktumId ->
            if (templates.any { faktumId.generertFra(it.faktumId) }) faktaMap.remove(faktumId)
        }
    }

    fun accept(visitor: SøknadVisitor) {
        person.accept(visitor)
        visitor.preVisit(this, prosessVersjon, uuid)
        this.forEach { it.accept(visitor) }
        visitor.postVisit(this, prosessVersjon, uuid)
    }
}
