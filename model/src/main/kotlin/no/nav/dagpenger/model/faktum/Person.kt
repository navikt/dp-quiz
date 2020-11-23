package no.nav.dagpenger.model.faktum

import java.util.Objects

class Person private constructor(
    internal val fnr: String,
    private val aktørId: String,
    private val søknader: MutableList<Søknad>
) {

    companion object {
        internal val prototype = Person("", "")
    }
    constructor(
        fnr: String,
        aktørId: String
    ) : this(fnr, aktørId, mutableListOf())

    override fun equals(other: Any?) =
        other is Person && this.fnr == other.fnr && this.aktørId == other.aktørId

    override fun hashCode() = Objects.hash(fnr, aktørId)
}
