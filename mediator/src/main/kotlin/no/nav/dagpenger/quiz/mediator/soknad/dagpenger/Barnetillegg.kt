package no.nav.dagpenger.quiz.mediator.soknad.dagpenger

import no.nav.dagpenger.model.factory.FaktumFactory
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.seksjon.Seksjon
import no.nav.dagpenger.model.subsumsjon.DeltreSubsumsjon
import no.nav.dagpenger.model.subsumsjon.alle
import no.nav.dagpenger.model.subsumsjon.deltre
import no.nav.dagpenger.quiz.mediator.soknad.DslFaktaseksjon

object Barnetillegg : DslFaktaseksjon {

    override val fakta: List<FaktumFactory<*>>
        get() = BarnetilleggSøker.fakta + BarnetilleggRegister.fakta

    override fun seksjon(søknad: Søknad): List<Seksjon> {
        return BarnetilleggSøker.seksjon(søknad) + BarnetilleggRegister.seksjon(søknad)
    }

    override fun regeltre(søknad: Søknad): DeltreSubsumsjon {
        return "barnetillegg fra søker og register".deltre {
            "barnetillegg fra søker og register må være tatt stilling til".alle(
                BarnetilleggRegister.regeltre(søknad),
                BarnetilleggSøker.regeltre(søknad)
            )
        }
    }
}
