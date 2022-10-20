package no.nav.dagpenger.quiz.mediator.soknad.innsending

import no.nav.dagpenger.model.faktum.Envalg
import no.nav.dagpenger.model.faktum.Faktum
import no.nav.dagpenger.model.faktum.GrunnleggendeFaktum
import no.nav.dagpenger.model.faktum.GyldigeValg
import no.nav.dagpenger.model.faktum.LandGrupper
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.model.visitor.SøknadprosessVisitor
import no.nav.dagpenger.quiz.mediator.behovløsere.MetadataStrategi
import no.nav.dagpenger.quiz.mediator.behovløsere.MetadataStrategi.Metadata

class InnsendingMetadataStrategi : MetadataStrategi {
    private fun Søknadprosess.innsendingSvar() = HenvendelseType(this).hva
    override fun metadata(søknadprosess: Søknadprosess) =
        Metadata("GENERELL_INNSENDING", søknadprosess.innsendingSvar())

    private class HenvendelseType(søknadprosess: Søknadprosess) : SøknadprosessVisitor {
        var hva: String? = null

        init {
            søknadprosess.accept(this)
        }

        override fun <R : Comparable<R>> visitMedSvar(
            faktum: GrunnleggendeFaktum<R>,
            tilstand: Faktum.FaktumTilstand,
            id: String,
            avhengigeFakta: Set<Faktum<*>>,
            avhengerAvFakta: Set<Faktum<*>>,
            godkjenner: Set<Faktum<*>>,
            roller: Set<Rolle>,
            clazz: Class<R>,
            svar: R,
            besvartAv: String?,
            gyldigeValg: GyldigeValg?,
            landGrupper: LandGrupper?
        ) {
            super.visitMedSvar(
                faktum,
                tilstand,
                id,
                avhengigeFakta,
                avhengerAvFakta,
                godkjenner,
                roller,
                clazz,
                svar,
                besvartAv,
                gyldigeValg,
                landGrupper
            )
            if (id != Hvorfor.`hvorfor vil du sende oss ting`.toString()) return
            hva = (svar as Envalg).single()
        }
    }
}
