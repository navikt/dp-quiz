package no.nav.dagpenger.model.faktum

import no.nav.dagpenger.model.visitor.FaktumVisitor

typealias LandGrupper = Map<String, List<Land>>

open class GrunnleggendeFaktum<R : Comparable<R>> internal constructor(
    faktumId: FaktumId,
    navn: String,
    private val clazz: Class<R>,
    avhengigeFakta: MutableSet<Faktum<*>>,
    avhengerAvFakta: MutableSet<Faktum<*>>,
    protected val godkjenner: MutableSet<Faktum<*>>,
    protected val sannsynliggjøringsFakta: MutableSet<Faktum<*>>,
    roller: MutableSet<Rolle>,
    private val gyldigeValg: GyldigeValg? = null,
    private val landGrupper: LandGrupper? = null
) : Faktum<R>(faktumId, navn, avhengigeFakta, avhengerAvFakta, roller) {
    private var tilstand: Tilstand = Ukjent
    protected lateinit var gjeldendeSvar: R
    private var besvartAv: Besvarer? = null

    private data class Besvarer(val ident: String)

    internal constructor(
        faktumId: FaktumId,
        navn: String,
        clazz: Class<R>,
        roller: MutableSet<Rolle> = mutableSetOf(),
        gyldigeValg: GyldigeValg? = null,
        landGrupper: LandGrupper? = null
    ) : this(
        faktumId = faktumId,
        navn = navn,
        clazz = clazz,
        avhengigeFakta = mutableSetOf(),
        avhengerAvFakta = mutableSetOf(),
        godkjenner = mutableSetOf(),
        sannsynliggjøringsFakta = mutableSetOf(),
        roller = roller,
        gyldigeValg = gyldigeValg,
        landGrupper = landGrupper
    )

    internal fun godkjenner(fakta: List<Faktum<*>>) = godkjenner.addAll(fakta)

    internal fun sannsynliggjøresAv(sannsynliggjøringsFakta: List<GrunnleggendeFaktum<Dokument>>) {

        println("$sannsynliggjøringsFakta sannsynligjør $this")
        this.sannsynliggjøringsFakta.addAll(sannsynliggjøringsFakta)
    }

    override fun type() = clazz

    override fun besvar(r: R, besvarer: String?) = this.apply {
        when (r) {
            is ValgteVerdier -> requireNotNull(gyldigeValg) { "Et valg faktum uten gyldigeValg?" }.sjekk(r)
        }
        super.besvar(r, besvarer)
        gjeldendeSvar = r
        tilstand = Kjent
        besvartAv = besvarer?.let { Besvarer(it) }
    }

    override fun rehydrer(r: R, besvarer: String?): Faktum<R> = this.apply {
        super.rehydrer(r, besvarer)
        gjeldendeSvar = r
        tilstand = Kjent
        besvartAv = besvarer?.let { Besvarer(it) }
    }

    override fun bygg(byggetFakta: MutableMap<FaktumId, Faktum<*>>): Faktum<*> {
        if (byggetFakta.containsKey(faktumId)) return byggetFakta[faktumId]!!

        return GrunnleggendeFaktum(
            faktumId,
            navn,
            clazz,
            mutableSetOf(),
            mutableSetOf(),
            mutableSetOf(),
            mutableSetOf(),
            roller,
            gyldigeValg,
            landGrupper
        )
            .also { nyttFaktum ->
                byggetFakta[faktumId] = nyttFaktum
                this.avhengigeFakta.forEach { nyttFaktum.avhengigeFakta.add(it.bygg(byggetFakta)) }
                this.avhengerAvFakta.forEach { nyttFaktum.avhengerAvFakta.add(it.bygg(byggetFakta)) }
                this.godkjenner.forEach { nyttFaktum.godkjenner.add(it.bygg(byggetFakta)) }
                this.sannsynliggjøringsFakta.forEach { nyttFaktum.sannsynliggjøringsFakta.add(it.bygg(byggetFakta)) }
            }
    }

    override fun svar(): R = tilstand.svar(this)
    override fun besvartAv(): String? = tilstand.besvartAv(this)

    override fun erBesvart() = tilstand == Kjent

    override fun tilUbesvart() {
        tilstand = Ukjent
        avhengigeFakta.filterIsInstance<GrunnleggendeFaktum<*>>().forEach { it.tilUbesvart() }
    }

    override fun accept(visitor: FaktumVisitor) {
        faktumId.accept(visitor)
        tilstand.accept(this, visitor)
        visitor.preVisitAvhengigeFakta(this, avhengigeFakta)
        visitor.postVisitAvhengigeFakta(this, avhengigeFakta)

        visitor.preVisitAvhengerAvFakta(this, avhengerAvFakta)
        visitor.postVisitAvhengerAvFakta(this, avhengerAvFakta)
    }

    override fun grunnleggendeFakta() = setOf(this)

    override fun leggTilHvis(kode: FaktumTilstand, fakta: MutableSet<GrunnleggendeFaktum<*>>) {
        if (tilstand.kode == kode) fakta.add(this)
    }

    override fun tilTemplate() = TemplateFaktum(faktumId, navn, clazz, gyldigeValg = gyldigeValg, roller = roller, landgrupper = landGrupper)

    protected open fun acceptUtenSvar(visitor: FaktumVisitor) {
        visitor.visitUtenSvar(
            faktum = this,
            tilstand = Ukjent.kode,
            id = id,
            avhengigeFakta = avhengigeFakta,
            avhengerAvFakta = avhengerAvFakta,
            godkjenner = godkjenner,
            sannsynliggjøringsFakta = sannsynliggjøringsFakta,
            roller = roller,
            clazz = clazz,
            gyldigeValg = gyldigeValg,
            landGrupper = landGrupper
        )
    }

    protected open fun acceptMedSvar(visitor: FaktumVisitor) {
        visitor.visitMedSvar(
            faktum = this,
            tilstand = Kjent.kode,
            id = id,
            avhengigeFakta = avhengigeFakta,
            avhengerAvFakta = avhengerAvFakta,
            godkjenner = godkjenner,
            sannsynliggjøringsFakta = sannsynliggjøringsFakta,
            roller = roller,
            clazz = clazz,
            svar = gjeldendeSvar,
            besvartAv = besvartAv?.ident,
            gyldigeValg = gyldigeValg,
            landGrupper = landGrupper
        )
    }

    private interface Tilstand {
        val kode: FaktumTilstand
        fun <R : Comparable<R>> accept(faktum: GrunnleggendeFaktum<R>, visitor: FaktumVisitor)
        fun <R : Comparable<R>> svar(faktum: GrunnleggendeFaktum<R>): R =
            throw IllegalStateException("Faktumet '$faktum' er ikke kjent enda")

        fun <R : Comparable<R>> besvartAv(faktum: GrunnleggendeFaktum<R>): String? =
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

        override fun <R : Comparable<R>> besvartAv(faktum: GrunnleggendeFaktum<R>): String? = faktum.besvartAv?.ident

        override fun <R : Comparable<R>> svar(faktum: GrunnleggendeFaktum<R>) = faktum.gjeldendeSvar
    }
}
