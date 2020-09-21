package no.nav.dagpenger.model.visitor

import no.nav.dagpenger.model.fakta.Faktum
import no.nav.dagpenger.model.fakta.GrunnleggendeFaktum
import no.nav.dagpenger.model.fakta.Rolle
import no.nav.dagpenger.model.fakta.UtledetFaktum
import no.nav.dagpenger.model.søknad.Seksjon
import no.nav.dagpenger.model.søknad.Søknad
import java.util.UUID

interface SøknadVisitor {
    fun preVisit(søknad: Søknad, uuid: UUID) {}
    fun postVisit(søknad: Søknad) {}
    fun preVisit(seksjon: Seksjon, fakta: Set<Faktum<*>>) {}
    fun postVisit(seksjon: Seksjon) {}
    fun <R : Comparable<R>> visit(faktum: GrunnleggendeFaktum<R>, tilstand: Faktum.FaktumTilstand, id: Int, avhengigeFakta: List<Faktum<*>>, roller: Set<Rolle>) {}
    fun <R : Comparable<R>> visit(faktum: GrunnleggendeFaktum<R>, tilstand: Faktum.FaktumTilstand, id: Int, avhengigeFakta: List<Faktum<*>>, roller: Set<Rolle>, svar: R) {}
    fun <R : Comparable<R>> preVisit(faktum: UtledetFaktum<R>, id: Int, avhengigeFakta: List<Faktum<*>>, children: Set<Faktum<*>>, svar: R) {}
    fun <R : Comparable<R>> preVisit(faktum: UtledetFaktum<R>, id: Int, avhengigeFakta: List<Faktum<*>>, children: Set<Faktum<*>>) {}
    fun <R : Comparable<R>> postVisit(faktum: UtledetFaktum<R>, id: Int, children: Set<Faktum<*>>) {}
}
