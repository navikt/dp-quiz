package no.nav.dagpenger.model.fakta

import java.time.LocalDate

interface TypedFaktum {
    infix fun id(rootId: Int): Faktum<*>
    infix fun id(id: String): Faktum<*>
    infix fun ja(rootId: Int): Faktum<Boolean>
    infix fun ja(id: String): Faktum<Boolean>
    infix fun dato(rootId: Int): Faktum<LocalDate>
    infix fun dato(id: String): Faktum<LocalDate>
}