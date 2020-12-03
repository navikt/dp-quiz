package no.nav.dagpenger.model.faktum

import no.nav.dagpenger.model.visitor.FaktumVisitor

open class GrunnleggendeFaktum<R : Comparable<R>> internal constructor(
    faktumId: FaktumId,
    navn: String,
    private val clazz: Class<R>,
    avhengigeFakta: MutableSet<Faktum<*>>,
    avhengerAvFakta: MutableSet<Faktum<*>>,
    private val godkjenner: MutableSet<Faktum<*>>,
    roller: MutableSet<Rolle>
) : Faktum<R>(faktumId, navn, avhengigeFakta, avhengerAvFakta, roller) {
    private var tilstand: Tilstand = Ukjent
    protected lateinit var gjeldendeSvar: R

    internal constructor(faktumId: FaktumId, navn: String, clazz: Class<R>) : this(
        faktumId,
        navn,
        clazz,
        mutableSetOf(),
        mutableSetOf(),
        mutableSetOf(),
        mutableSetOf()
    )

    internal fun godkjenner(fakta: List<Faktum<*>>) = godkjenner.addAll(fakta)

    override fun clazz() = clazz

    override fun besvar(r: R) = this.apply {
        super.besvar(r)
        gjeldendeSvar = r
        tilstand = Kjent
    }

    override fun bygg(byggetFakta: MutableMap<FaktumId, Faktum<*>>): Faktum<*> {
        if (byggetFakta.containsKey(faktumId)) return byggetFakta[faktumId]!!

        return GrunnleggendeFaktum(faktumId, navn, clazz, mutableSetOf(), mutableSetOf(), mutableSetOf(), roller)
            .also { nyttFaktum ->
                byggetFakta[faktumId] = nyttFaktum
                this.avhengigeFakta.forEach { nyttFaktum.avhengigeFakta.add(it.bygg(byggetFakta)) }
                this.avhengerAvFakta.forEach { nyttFaktum.avhengerAvFakta.add(it.bygg(byggetFakta)) }
                this.godkjenner.forEach { nyttFaktum.godkjenner.add(it.bygg(byggetFakta)) }
            }
    }

    override fun svar(): R = tilstand.svar(this)

    override fun erBesvart() = tilstand == Kjent

    override fun tilUbesvart() {
        tilstand = Ukjent
        avhengigeFakta.filterIsInstance<GrunnleggendeFaktum<*>>().forEach { it.tilUbesvart() }
    }

    override fun accept(visitor: FaktumVisitor) {
        faktumId.accept(visitor)
        tilstand.accept(this, visitor)
        visitor.preVisitAvhengerAvFakta(this, avhengerAvFakta)
        visitor.postVisitAvhengerAvFakta(this, avhengerAvFakta)
    }

    override fun grunnleggendeFakta() = setOf(this)

    override fun leggTilHvis(kode: FaktumTilstand, fakta: MutableSet<GrunnleggendeFaktum<*>>) {
        if (tilstand.kode == kode) fakta.add(this)
    }

    override fun tilTemplate() = TemplateFaktum(faktumId, navn, clazz)

    protected open fun acceptUtenSvar(visitor: FaktumVisitor) {
        visitor.visit(this, Ukjent.kode, id, avhengigeFakta, avhengerAvFakta, godkjenner, roller, clazz)
    }

    protected open fun acceptMedSvar(visitor: FaktumVisitor) {
        visitor.visit(this, Kjent.kode, id, avhengigeFakta, avhengerAvFakta, godkjenner, roller, clazz, gjeldendeSvar)
    }

    private interface Tilstand {
        val kode: FaktumTilstand

        fun <R : Comparable<R>> accept(faktum: GrunnleggendeFaktum<R>, visitor: FaktumVisitor)
        fun <R : Comparable<R>> svar(faktum: GrunnleggendeFaktum<R>): R =
            throw IllegalStateException("Faktumet er ikke kjent enda")
    }

    private object Ukjent : Tilstand {
        override val kode = FaktumTilstand.Ukjent
        override fun <R : Comparable<R>> accept(faktum: GrunnleggendeFaktum<R>, visitor: FaktumVisitor) {
            faktum.acceptUtenSvar(visitor)
        }
    }

    private object Kjent : Tilstand {
        override val kode = FaktumTilstand.Kjent
        override fun <R : Comparable<R>> accept(faktum: GrunnleggendeFaktum<R>, visitor: FaktumVisitor) {
            faktum.acceptMedSvar(visitor)
        }

        override fun <R : Comparable<R>> svar(faktum: GrunnleggendeFaktum<R>) = faktum.gjeldendeSvar
    }
}
