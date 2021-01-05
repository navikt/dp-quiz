package no.nav.dagpenger.model.helpers

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.boolsk
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dato
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dokument
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.heltall
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.inntekt
import no.nav.dagpenger.model.factory.UtledetFaktumFactory.Companion.maks
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.regel.av
import no.nav.dagpenger.model.regel.er
import no.nav.dagpenger.model.regel.etter
import no.nav.dagpenger.model.regel.før
import no.nav.dagpenger.model.regel.gyldigGodkjentAv
import no.nav.dagpenger.model.regel.med
import no.nav.dagpenger.model.regel.minst
import no.nav.dagpenger.model.regel.under
import no.nav.dagpenger.model.seksjon.Seksjon
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.model.seksjon.Versjon
import no.nav.dagpenger.model.seksjon.Versjon.UserInterfaceType.Web
import no.nav.dagpenger.model.subsumsjon.Subsumsjon
import no.nav.dagpenger.model.subsumsjon.alle
import no.nav.dagpenger.model.subsumsjon.eller
import no.nav.dagpenger.model.subsumsjon.makro
import no.nav.dagpenger.model.subsumsjon.minstEnAv
import no.nav.dagpenger.model.subsumsjon.så
import no.nav.dagpenger.model.subsumsjon.uansett

private val f1Factory = boolsk faktum "f1" id 1
private val f2Factory = dato faktum "f2" id 2
private val f3Factory = dato faktum "f3" id 3
private val f4Factory = dato faktum "f4" id 4
private val f5Factory = dato faktum "f5" id 5
private val f6Factory = inntekt faktum "f6" id 6
private val f7Factory = inntekt faktum "f7" id 7
private val f8Factory = inntekt faktum "f8" id 8
private val f9Factory = inntekt faktum "f9" id 9
private val f10Factory = boolsk faktum "f10" id 10
private val f11Factory = dokument faktum "f11" id 11
private val f12Factory = boolsk faktum "f12" id 12 avhengerAv 11
private val f13Factory = dato faktum "f13" id 13
private val f14Factory = boolsk faktum "f14" id 14
private val f15Factory = heltall faktum "f15" id 15
private val f16Factory = heltall faktum "f16" id 16
private val f17Factory = boolsk faktum "f17" id 17
private val f18Factory = boolsk faktum "f18" id 18
private val f19Factory = boolsk faktum "f19" id 19 avhengerAv 2 og 13
private val f345Factory = maks dato "345" av 3 og 4 og 5 id 345

private val prototypeSøknad1 = Søknad(
    0,
    f1Factory,
    f2Factory,
    f3Factory,
    f4Factory,
    f5Factory,
    f6Factory,
    f7Factory,
    f8Factory,
    f9Factory,
    f10Factory,
    f11Factory,
    f12Factory avhengerAv f11Factory,
    f13Factory,
    f14Factory,
    f15Factory genererer f16Factory og f17Factory og f18Factory,
    f16Factory,
    f17Factory,
    f18Factory,
    f19Factory avhengerAv 2 og 13,
    f345Factory
)

private val p1Boolean = prototypeSøknad1 boolsk f1Factory
private val p2Dato = prototypeSøknad1 dato f2Factory
private val p3Dato = prototypeSøknad1 dato f3Factory
private val p4Dato = prototypeSøknad1 dato f4Factory
private val p5Dato = prototypeSøknad1 dato f5Factory
private val p_3_4_5Dato = prototypeSøknad1 dato f345Factory
private val p6Inntekt = prototypeSøknad1 inntekt f6Factory
private val p7Inntekt = prototypeSøknad1 inntekt f7Factory
private val p8Inntekt = prototypeSøknad1 inntekt f8Factory
private val p9Inntekt = prototypeSøknad1 inntekt f9Factory
private val p10Boolean = prototypeSøknad1 boolsk f10Factory
private val p11Dokument = prototypeSøknad1 dokument f11Factory
private val p12Boolean = prototypeSøknad1 boolsk f12Factory
private val p13Dato = prototypeSøknad1 dato f13Factory
private val p14Boolean = prototypeSøknad1 boolsk f14Factory
private val p15Int = prototypeSøknad1 generator f15Factory
private val p16Int = prototypeSøknad1 heltall f16Factory
private val p17Boolean = prototypeSøknad1 boolsk f17Factory
private val p18Boolean = prototypeSøknad1 boolsk f18Factory
private val p19Boolean = prototypeSøknad1 boolsk f19Factory

private val datosjekk = "datosjekk".alle(
    p1Boolean er true,
    p2Dato etter p_3_4_5Dato,
    p3Dato før p4Dato
)

private val dokumentOpplastning = "dokumentopplastning" makro (
    p10Boolean er true eller (p12Boolean av p11Dokument)
    )

private val inntektValidering = "inntektvalidering".minstEnAv(
    p6Inntekt minst p8Inntekt,
    p7Inntekt minst p9Inntekt
)

private val alderSjekk = "aldersjekk" makro (
    p16Int under 18 så (p17Boolean er true)
    )

private val personerGodkjenning = p15Int med alderSjekk uansett (p14Boolean er true)

/* ktlint-disable parameter-list-wrapping */
private val prototypeSubsumsjon =
    datosjekk så (
        dokumentOpplastning så (
            inntektValidering så (
                personerGodkjenning
                )
            ) eller (
            (p2Dato etter p13Dato) gyldigGodkjentAv p19Boolean
            )
        )

private val prototypeSeksjon1 = Seksjon("seksjon1", Rolle.nav, p1Boolean, p2Dato)
private val prototypeSeksjon2 = Seksjon("seksjon2", Rolle.nav, p6Inntekt, p7Inntekt, p8Inntekt, p9Inntekt)
private val prototypeSeksjon3 = Seksjon("seksjon3", Rolle.nav, p15Int, p16Int)
private val prototypeSeksjon4 = Seksjon("seksjon4", Rolle.søker, p3Dato, p4Dato, p5Dato, p_3_4_5Dato, p13Dato)
private val prototypeSeksjon5 = Seksjon("seksjon5", Rolle.søker, p10Boolean, p11Dokument)
private val prototypeSeksjon6 = Seksjon("seksjon6", Rolle.søker, p15Int)
private val prototypeSeksjon7 = Seksjon("seksjon7", Rolle.søker, p17Boolean, p18Boolean)
private val prototypeSeksjon8 =
    Seksjon("seksjon8", Rolle.saksbehandler, p6Inntekt, p7Inntekt, p12Boolean, p14Boolean, p16Int, p19Boolean)
private val prototypeSeksjon9 =
    Seksjon("seksjon9", Rolle.saksbehandler, p3Dato, p4Dato, p5Dato, p_3_4_5Dato, p13Dato)

private val webPrototypeFaktagrupper: Søknadprosess =
    Søknadprosess(
        prototypeSeksjon1,
        prototypeSeksjon2,
        prototypeSeksjon3,
        prototypeSeksjon4,
        prototypeSeksjon5,
        prototypeSeksjon6,
        prototypeSeksjon7,
        prototypeSeksjon8,
        prototypeSeksjon9
    )

internal lateinit var seksjon1: Seksjon
internal lateinit var seksjon2: Seksjon
internal lateinit var seksjon3: Seksjon
internal lateinit var seksjon4: Seksjon
internal lateinit var seksjon5: Seksjon
internal lateinit var seksjon6: Seksjon
internal lateinit var seksjon7: Seksjon
internal lateinit var seksjon8: Seksjon
internal lateinit var rootSubsumsjon: Subsumsjon

private val søknadprosessTestBygger = Versjon.Bygger(prototypeSøknad1, prototypeSubsumsjon, mapOf(Web to webPrototypeFaktagrupper))

internal class NyttEksempel() {

    internal val søknadprosess: Søknadprosess by lazy {

        søknadprosessTestBygger.søknadprosess(testPerson, Web).also {
            seksjon1 = it[0]
            seksjon2 = it[1]
            seksjon3 = it[2]
            seksjon4 = it[3]
            seksjon5 = it[4]
            seksjon6 = it[5]
            seksjon7 = it[6]
            seksjon8 = it[7]
            rootSubsumsjon = it.rootSubsumsjon
        }
    }
}
