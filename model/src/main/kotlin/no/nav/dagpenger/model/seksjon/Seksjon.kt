package no.nav.dagpenger.model.seksjon

import no.nav.dagpenger.model.faktum.Fakta
import no.nav.dagpenger.model.faktum.Faktum
import no.nav.dagpenger.model.faktum.FaktumId
import no.nav.dagpenger.model.faktum.GrunnleggendeFaktum
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.TemplateFaktum
import no.nav.dagpenger.model.subsumsjon.Subsumsjon
import no.nav.dagpenger.model.visitor.UtredningsprosessVisitor

class Seksjon private constructor(
    val navn: String,
    private val rolle: Rolle,
    private val seksjonFakta: MutableSet<Faktum<*>>,
    private val avhengerAvFakta: MutableSet<Faktum<*>>,
    val indeks: Int = 0,
) : MutableSet<Faktum<*>> by seksjonFakta {
    private lateinit var _utredningsprosess: Utredningsprosess
    private val genererteSeksjoner = mutableListOf<Seksjon>()

    init {
        seksjonFakta.toSet().forEach {
            it.add(rolle)
            it.add(this)
            it.leggTilAvhengigheter(avhengerAvFakta)
        }
    }

    companion object {
        internal fun List<Seksjon>.saksbehandlerSeksjoner(relevanteFakta: Set<Faktum<*>>) =
            this.filter { it.rolle == Rolle.saksbehandler }.map {
                it.filtrertSeksjon(relevanteFakta)
            }

        internal fun Collection<Seksjon>.brukerSeksjoner() =
            this.filter { it.rolle == Rolle.søker }

        internal fun List<Seksjon>.medSøknadprosess() = filter { it::_utredningsprosess.isInitialized }
    }

    constructor(navn: String, rolle: Rolle, vararg fakta: Faktum<*>) : this(
        navn,
        rolle,
        fakta.toMutableSet(),
        mutableSetOf<Faktum<*>>(),
    )

    internal fun filtrertSeksjon(subsumsjon: Subsumsjon) = filtrertSeksjon(subsumsjon.relevanteFakta())

    private fun filtrertSeksjon(relevanteFakta: Set<Faktum<*>>) =
        Seksjon(
            navn,
            rolle,
            seksjonFakta.filter { faktum -> faktum.erBesvart() || faktum in relevanteFakta }.toMutableSet(),
            avhengerAvFakta.filter { faktum -> faktum.erBesvart() || faktum in relevanteFakta }.toMutableSet(),
        ).also { it.søknadprosess(_utredningsprosess) }

    internal fun gjeldendeFakta(subsumsjon: Subsumsjon) = filtrertSeksjon(subsumsjon.nesteFakta())

    internal operator fun contains(nesteFakta: Set<GrunnleggendeFaktum<*>>) =
        nesteFakta.any { it in seksjonFakta }

    internal fun søknadprosess(utredningsprosess: Utredningsprosess) {
        this._utredningsprosess = utredningsprosess
    }

    internal fun bareTemplates() = seksjonFakta.all { it is TemplateFaktum }

    internal fun deepCopy(indeks: Int): Seksjon {
        return if (indeks <= genererteSeksjoner.size) {
            genererteSeksjoner[indeks - 1]
        } else {
            Seksjon(navn, rolle, mutableSetOf(), avhengerAvFakta.toMutableSet(), indeks).also {
                _utredningsprosess.add(_utredningsprosess.indexOf(this) + indeks, it)
                genererteSeksjoner.add(it)
                it.søknadprosess(_utredningsprosess)
            }
        }
    }

    fun accept(visitor: UtredningsprosessVisitor) {
        visitor.preVisit(this, rolle, seksjonFakta, indeks)
        seksjonFakta.sorted().forEach { it.accept(visitor) }
        visitor.preVisitAvhengerAv(this, avhengerAvFakta)
        avhengerAvFakta.sorted().forEach { it.accept(visitor) }
        visitor.postVisitAvhengerAv(this, avhengerAvFakta)
        visitor.postVisit(this, rolle, indeks)
    }

    internal fun add(faktum: GrunnleggendeFaktum<*>): Boolean =
        _utredningsprosess.idOrNull(faktum.faktumId).let { eksisterendeFaktum ->
            (eksisterendeFaktum == null).also {
                if (it) { // Use existing Faktum
                    seksjonFakta.add(faktum)
                    _utredningsprosess.add(faktum)
                } else { // Use new Faktum
                    seksjonFakta.add(eksisterendeFaktum as GrunnleggendeFaktum<*>)
                }
            }
        }

    internal fun bygg(fakta: Fakta) = Seksjon(
        navn,
        rolle,
        seksjonFakta
            .map { fakta.id(it.faktumId) }
            .toMutableSet(),
        avhengerAvFakta
            .map { fakta.id(it.faktumId) }
            .toMutableSet(),
    )

    internal fun tilbakestill(templateFaktumId: FaktumId) {
        seksjonFakta.removeIf { it.faktumId.generertFra(templateFaktumId) }
        genererteSeksjoner.toSet().forEach {
            it.tilbakestill(templateFaktumId)
            if (it.isEmpty()) genererteSeksjoner.remove(it)
        }
        _utredningsprosess.removeIf { it.isEmpty() }
    }

    fun somSpørsmål() = rolle.spørsmål(_utredningsprosess, navn)
}
