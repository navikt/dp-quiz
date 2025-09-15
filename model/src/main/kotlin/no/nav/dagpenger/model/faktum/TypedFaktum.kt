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

    infix fun envalg(rootId: Int): Faktum<Envalg>

    infix fun envalg(id: String): Faktum<Envalg>

    infix fun flervalg(rootId: Int): Faktum<Flervalg>

    infix fun flervalg(id: String): Faktum<Flervalg>

    infix fun tekst(rootId: Int): Faktum<Tekst>

    infix fun tekst(id: String): Faktum<Tekst>

    infix fun periode(rootId: Int): Faktum<Periode>

    infix fun periode(id: String): Faktum<Periode>

    infix fun land(rootId: Int): Faktum<Land>

    infix fun land(id: String): Faktum<Land>
}
