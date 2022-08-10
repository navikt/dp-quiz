package no.nav.dagpenger.model.subsumsjon

import no.nav.dagpenger.model.faktum.Dokument
import no.nav.dagpenger.model.faktum.Faktum
import no.nav.dagpenger.model.faktum.GrunnleggendeFaktum
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.regel.Regel
import no.nav.dagpenger.model.regel.sannsynliggjøresAv
import no.nav.dagpenger.model.seksjon.Søknadprosess

class DokumentasjonskravSubsumsjon private constructor(
    navn: String,
    private val child: Subsumsjon,
    private val dokumentopplastning: Subsumsjon,
    private val sannsynliggjøringsFakta: GrunnleggendeFaktum<Dokument>,
    private val godkjenningsfakta: List<GrunnleggendeFaktum<Boolean>>,
) : GodkjenningsSubsumsjon(
    navn, Action.UansettAction, dokumentopplastning, godkjenningsfakta
) {

    init {
        require(child.alleFakta().all { it is GrunnleggendeFaktum }) { "SannsynliggjøringsFakta er kun støttet for GrunnleggendeFaktum" }
        child.alleFakta().forEach { (it as GrunnleggendeFaktum).sannsynliggjøresAv(listOf(sannsynliggjøringsFakta)) }
    }

    internal constructor(
        child: Subsumsjon,
        sannsynliggjøringsFakta: GrunnleggendeFaktum<Dokument>,
        godkjenningsFakta: List<GrunnleggendeFaktum<Boolean>>
    ) :
        this(
            "${child.navn} dokumentasjonskrav", child,
            EnkelSubsumsjon(
                object : Regel {
                    override val typeNavn = "dokumentopplastning"
                    override fun resultat(fakta: List<Faktum<*>>) = true
                    override fun toString(fakta: List<Faktum<*>>) = "Sjekk at dokument `${fakta[0]}` er opplastet"
                },
                sannsynliggjøringsFakta
            ),
            sannsynliggjøringsFakta, godkjenningsFakta
        )

    override fun lokaltResultat(): Boolean? = when (child.resultat()) {
        true -> {
            when (dokumentopplastning.resultat()) {
                true -> super.lokaltResultat()
                else -> true
            }
        }
        else -> child.resultat()
    }

    override fun deepCopy(): Subsumsjon {
        return DokumentasjonskravSubsumsjon(
            navn,
            child.deepCopy(),
            dokumentopplastning.deepCopy(),
            sannsynliggjøringsFakta,
            godkjenningsfakta,
        )
    }

    override fun deepCopy(indeks: Int, søknad: Søknad): Subsumsjon {
        return DokumentasjonskravSubsumsjon(
            "$navn [$indeks]",
            child.deepCopy(indeks, søknad),
            dokumentopplastning.deepCopy(indeks, søknad),
            sannsynliggjøringsFakta,
            godkjenningsfakta,
        )
    }

    override fun bygg(søknad: Søknad) = DokumentasjonskravSubsumsjon(
        navn,
        child.bygg(søknad),
        dokumentopplastning.bygg(søknad),
        søknad.dokument(sannsynliggjøringsFakta.id) as GrunnleggendeFaktum<Dokument>,
        godkjenningsfakta.map { søknad.boolsk(it.id) as GrunnleggendeFaktum<Boolean> },
    )

    override fun deepCopy(søknadprosess: Søknadprosess) = DokumentasjonskravSubsumsjon(
        navn,
        child.deepCopy(søknadprosess),
        dokumentopplastning.deepCopy(søknadprosess),
        søknadprosess.dokument(sannsynliggjøringsFakta.id) as GrunnleggendeFaktum<Dokument>,
        godkjenningsfakta.map { søknadprosess.boolsk(it.id) as GrunnleggendeFaktum<Boolean> },
    )
}
