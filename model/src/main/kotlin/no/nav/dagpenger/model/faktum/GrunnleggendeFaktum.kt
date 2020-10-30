package no.nav.dagpenger.model.faktum

import no.nav.dagpenger.model.visitor.FaktumVisitor

open class GrunnleggendeFaktum<R : Comparable<R>> internal constructor(
    faktumId: FaktumId,
    navn: String,
    private val clazz: Class<R>,
    avhengigeFakta: MutableSet<Faktum<*>>,
    avhengerAvFakta: MutableSet<Faktum<*>>,
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
        mutableSetOf()
    )

    override fun clazz() = clazz

    override fun besvar(r: R, rolle: Rolle) = this.apply {
        super.besvar(r, rolle)
        gjeldendeSvar = r
        tilstand = Kjent
    }

    override fun bygg(byggetFakta: MutableMap<FaktumId, Faktum<*>>): Faktum<*> {
        if (byggetFakta.containsKey(faktumId)) return byggetFakta[faktumId]!!
        val avhengigheter = avhengigeFakta.map { it.bygg(byggetFakta) }.toMutableSet()

        return GrunnleggendeFaktum(faktumId, navn, clazz, avhengigheter, avhengerAvFakta, roller)
            .also { byggetFakta[faktumId] = it }
    }

    override fun svar(): R = tilstand.svar(this)

    override fun erBesvart() = tilstand == Kjent

    override fun tilUbesvart() {
        tilstand = Ukjent
    }

    override fun accept(visitor: FaktumVisitor) {
        faktumId.accept(visitor)
        tilstand.accept(this, visitor)
    }

    override fun grunnleggendeFakta() = setOf(this)

    override fun leggTilHvis(kode: FaktumTilstand, fakta: MutableSet<GrunnleggendeFaktum<*>>) {
        if (tilstand.kode == kode) fakta.add(this)
    }

    override fun tilTemplate() = TemplateFaktum(faktumId, navn, clazz)

    protected open fun acceptUtenSvar(visitor: FaktumVisitor) {
        visitor.visit(this, Ukjent.kode, id, avhengigeFakta, avhengerAvFakta, roller, clazz)
    }

    protected open fun acceptMedSvar(visitor: FaktumVisitor) {
        visitor.visit(this, Kjent.kode, id, avhengigeFakta, avhengerAvFakta, roller, clazz, gjeldendeSvar)
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
