package no.nav.dagpenger.model.fakta

interface Faktum<R: Any> {
    infix fun besvar(r: R): Faktum<R>
    fun svar(): R
    fun leggTilHvis(kode: FaktumTilstand, fakta: MutableSet<GrunnleggendeFaktum<*>>)

    enum class FaktumTilstand {
        Ukjent,
        Kjent
    }
}