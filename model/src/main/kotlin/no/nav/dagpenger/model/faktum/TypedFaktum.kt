package no.nav.dagpenger.model.faktum

import java.time.LocalDate

interface TypedFaktum {
    infix fun id(rootId: Int): Faktum<*>
    infix fun id(id: String): Faktum<*>
    infix fun boolsk(rootId: Int): Faktum<Boolean>
    infix fun boolsk(id: String): Faktum<Boolean>
    infix fun dato(rootId: Int): Faktum<LocalDate>
    infix fun dato(id: String): Faktum<LocalDate>
    infix fun inntekt(rootId: Int): Faktum<Inntekt>
    infix fun inntekt(id: String): Faktum<Inntekt>
    infix fun dokument(rootId: Int): Faktum<Dokument>
    infix fun dokument(id: String): Faktum<Dokument>
    infix fun heltall(rootId: Int): Faktum<Int>
    infix fun heltall(id: String): Faktum<Int>
    infix fun desimaltall(rootId: Int): Faktum<Double>
    infix fun desimaltall(id: String): Faktum<Double>
    infix fun generator(rootId: Int): Faktum<Int>
    infix fun generator(id: String): Faktum<Int>
    infix fun valg(rootId: Int): ValgFaktum
    infix fun valg(id: String): ValgFaktum
}
