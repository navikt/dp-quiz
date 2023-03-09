package no.nav.dagpenger.model.helpers

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.boolsk
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dato
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dokument
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.heltall
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.inntekt
import no.nav.dagpenger.model.factory.UtledetFaktumFactory.Companion.maks
import no.nav.dagpenger.model.faktum.Fakta
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.regel.dokumenteresAv
import no.nav.dagpenger.model.regel.er
import no.nav.dagpenger.model.regel.etter
import no.nav.dagpenger.model.regel.før
import no.nav.dagpenger.model.regel.med
import no.nav.dagpenger.model.regel.minst
import no.nav.dagpenger.model.regel.under
import no.nav.dagpenger.model.seksjon.Prosess
import no.nav.dagpenger.model.seksjon.Seksjon
import no.nav.dagpenger.model.subsumsjon.alle
import no.nav.dagpenger.model.subsumsjon.deltre
import no.nav.dagpenger.model.subsumsjon.hvisIkkeOppfylt
import no.nav.dagpenger.model.subsumsjon.hvisOppfylt
import no.nav.dagpenger.model.subsumsjon.minstEnAv
import no.nav.dagpenger.model.subsumsjon.oppfyltGodkjentAv
import no.nav.dagpenger.model.subsumsjon.uansett

internal class NyttEksempel {
    private val testProsess = testProsesstype()
    private val prototypeFakta = Fakta(
        testProsess.faktaversjon,
        boolsk faktum "f1" id 1,
        dato faktum "f2" id 2,
        dato faktum "f3" id 3,
        dato faktum "f4" id 4,
        dato faktum "f5" id 5,
        inntekt faktum "f6" id 6,
        inntekt faktum "f7" id 7,
        inntekt faktum "f8" id 8,
        inntekt faktum "f9" id 9,
        boolsk faktum "f10" id 10,
        dokument faktum "f11" id 11,
        boolsk faktum "f12" id 12 avhengerAv 11,
        dato faktum "f13" id 13,
        boolsk faktum "f14" id 14,
        heltall faktum "f15" id 15 genererer 16 og 17 og 18,
        heltall faktum "f16" id 16,
        boolsk faktum "f17" id 17,
        boolsk faktum "f18" id 18,
        boolsk faktum "f19" id 19 avhengerAv 2 og 13,
        maks dato "345" av 3 og 4 og 5 id 345,
    )
    private val p1Boolean = prototypeFakta boolsk 1
    private val p2Dato = prototypeFakta dato 2
    private val p3Dato = prototypeFakta dato 3
    private val p4Dato = prototypeFakta dato 4
    private val p5Dato = prototypeFakta dato 5
    private val p_3_4_5Dato = prototypeFakta dato 345
    private val p6Inntekt = prototypeFakta inntekt 6
    private val p7Inntekt = prototypeFakta inntekt 7
    private val p8Inntekt = prototypeFakta inntekt 8
    private val p9Inntekt = prototypeFakta inntekt 9
    private val p10Boolean = prototypeFakta boolsk 10
    private val p11Dokument = prototypeFakta dokument 11
    private val p12Boolean = prototypeFakta boolsk 12
    private val p13Dato = prototypeFakta dato 13
    private val p14Boolean = prototypeFakta boolsk 14
    private val p15Int = prototypeFakta generator 15
    private val p16Int = prototypeFakta heltall 16
    private val p17Boolean = prototypeFakta boolsk 17
    private val p18Boolean = prototypeFakta boolsk 18
    private val p19Boolean = prototypeFakta boolsk 19
    private val datosjekk = "datosjekk".alle(
        p1Boolean er true,
        p2Dato etter p_3_4_5Dato,
        p3Dato før p4Dato,
    )
    private val dokumentOpplastning = "dokumentopplastning" deltre {
        p10Boolean er true hvisIkkeOppfylt { p12Boolean dokumenteresAv p11Dokument }
    }
    private val inntektValidering = "inntektvalidering".minstEnAv(
        p6Inntekt minst p8Inntekt,
        p7Inntekt minst p9Inntekt,
    )
    private val alderSjekk = "aldersjekk" deltre {
        p16Int under 18 hvisOppfylt { p17Boolean er true }
    }
    private val personerGodkjenning = p15Int med alderSjekk uansett { p14Boolean er true }

    /* ktlint-disable parameter-list-wrapping */
    private val prototypeSubsumsjon =
        datosjekk hvisOppfylt {
            dokumentOpplastning hvisOppfylt {
                inntektValidering hvisOppfylt {
                    personerGodkjenning
                }
            } hvisIkkeOppfylt {
                (p2Dato etter p13Dato).oppfyltGodkjentAv(p19Boolean)
            }
        }
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
    private val prototypeProsess: Prosess =
        Prosess(
            testProsess,
            prototypeFakta,
            prototypeSeksjon1,
            prototypeSeksjon2,
            prototypeSeksjon3,
            prototypeSeksjon4,
            prototypeSeksjon5,
            prototypeSeksjon6,
            prototypeSeksjon7,
            prototypeSeksjon8,
            prototypeSeksjon9,
            rootSubsumsjon = prototypeSubsumsjon,
        )
    internal val prosess: Prosess by lazy {
        prototypeProsess.testProsess()
    }
}
