package no.nav.dagpenger.quiz.mediator.integration

import no.nav.dagpenger.model.faktum.Inntekt.Companion.årlig
import no.nav.dagpenger.model.helpers.desember
import no.nav.dagpenger.model.helpers.januar
import no.nav.dagpenger.quiz.mediator.db.FaktumTable
import no.nav.dagpenger.quiz.mediator.db.ResultatRecord
import no.nav.dagpenger.quiz.mediator.db.SøknadRecord
import no.nav.dagpenger.quiz.mediator.helpers.Postgres
import no.nav.dagpenger.quiz.mediator.meldinger.AvslagPåMinsteinntektService
import no.nav.dagpenger.quiz.mediator.meldinger.FaktumSvarService
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.antallEndredeArbeidsforhold
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.behandlingsdato
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.eøsArbeid
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.fangstOgFiskInntektSiste36mnd
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.grunnbeløp
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.harHattDagpengerSiste36mnd
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.harInntektNesteKalendermåned
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.hattLukkedeSakerSiste8Uker
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.helseTilAlleTyperJobb
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.inntektSiste12mnd
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.inntektSiste36mnd
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.inntektsrapporteringsperiodeTom
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.jobbetUtenforNorge
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.kanJobbeDeltid
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.kanJobbeHvorSomHelst
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.lønnsgaranti
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.minsteinntektfaktor12mnd
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.minsteinntektfaktor36mnd
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.ordinær
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.over67årFradato
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.permittert
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.permittertFiskeforedling
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.registrertArbeidssøkerPeriodeFom
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.registrertArbeidssøkerPeriodeTom
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.registrertArbeidssøkerPerioder
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.senesteMuligeVirkningsdato
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.sykepengerSiste36mnd
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.søknadstidspunkt
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.verneplikt
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.villigTilÅBytteYrke
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett.ønsketDato
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class AvslagPåMinsteinntektTest : SøknadBesvarer() {

    @BeforeEach
    fun setup() {
        Postgres.withMigratedDb {
            AvslagPåMinsteinntektOppsett.registrer { prototypeSøknad -> FaktumTable(prototypeSøknad) }
            val søknadPersistence = SøknadRecord()
            val resultatPersistence = ResultatRecord()
            testRapid = TestRapid().also {
                FaktumSvarService(
                    søknadPersistence = søknadPersistence,
                    resultatPersistence = resultatPersistence,
                    rapidsConnection = it
                )
                AvslagPåMinsteinntektService(søknadPersistence, it)
            }
        }
    }

    @Test
    fun `De som har virkningsdato for langt fram i tid går til manuell`() {
        withSøknad(søknadFraInnsending) { besvar ->
            besvar(behandlingsdato, 5.januar)
            besvar(ønsketDato, 5.desember)
            besvar(søknadstidspunkt, 5.desember)
            besvar(senesteMuligeVirkningsdato, 19.januar)
            assertGjeldendeSeksjon("arbeidsforhold")
            besvar(
                antallEndredeArbeidsforhold,
                listOf(
                    listOf(
                        "$ordinær.1" to false,
                        "$permittert.1" to true,
                        "$lønnsgaranti.1" to false,
                        "$permittertFiskeforedling.1" to false
                    )
                )
            )
            assertGjeldendeSeksjon("virkningstidspunkt vi ikke kan håndtere")
        }
    }

    @Test
    fun `De som ikke oppfyller kravet til minsteinntekt får avslag`() {
        withSøknad(søknadFraInnsending) { besvar ->

            assertGjeldendeSeksjon("arbeidsforhold")
            besvar(
                antallEndredeArbeidsforhold,
                listOf(
                    listOf(
                        "$ordinær.1" to false,
                        "$permittert.1" to true,
                        "$lønnsgaranti.1" to false,
                        "$permittertFiskeforedling.1" to false
                    ),
                    listOf(
                        "$ordinær.2" to false,
                        "$permittert.2" to true,
                        "$lønnsgaranti.2" to false,
                        "$permittertFiskeforedling.2" to false
                    ),

                )
            )

            besvar(behandlingsdato, 5.januar)
            besvar(senesteMuligeVirkningsdato, 19.januar)

            assertGjeldendeSeksjon("datafrasøknad")
            besvar(eøsArbeid, false)
            besvar(jobbetUtenforNorge, false)
            besvar(fangstOgFiskInntektSiste36mnd, false)
            besvar(verneplikt, false)
            besvar(ønsketDato, 5.januar)
            besvar(søknadstidspunkt, 2.januar)
            besvar(harInntektNesteKalendermåned, false)
            besvar(kanJobbeDeltid, true)
            besvar(helseTilAlleTyperJobb, true)
            besvar(villigTilÅBytteYrke, true)
            besvar(kanJobbeHvorSomHelst, true)

            assertGjeldendeSeksjon("alder")
            besvar(over67årFradato, 1.desember)

            assertGjeldendeSeksjon("inntektsrapporteringsperioder")
            besvar(inntektsrapporteringsperiodeTom, 10.januar)

            assertGjeldendeSeksjon("inntektshistorikk")
            besvar(fangstOgFiskInntektSiste36mnd, false)

            assertGjeldendeSeksjon("dagpengehistorikk")
            besvar(harHattDagpengerSiste36mnd, false)
            besvar(hattLukkedeSakerSiste8Uker, false)

            assertGjeldendeSeksjon("sykepengehistorikk")
            besvar(sykepengerSiste36mnd, false)

            assertGjeldendeSeksjon("arbeidsøkerperioder")
            besvar(
                registrertArbeidssøkerPerioder,
                listOf(
                    listOf(
                        "$registrertArbeidssøkerPeriodeFom.1" to 1.januar(2018),
                        "$registrertArbeidssøkerPeriodeTom.1" to 30.januar(2018)
                    )
                )
            )

            assertGjeldendeSeksjon("minsteinntektKonstanter")
            besvar(minsteinntektfaktor36mnd, 1.5)
            besvar(minsteinntektfaktor12mnd, 3.0)
            besvar(grunnbeløp, 100000.årlig)

            assertGjeldendeSeksjon("inntekter")
            besvar(inntektSiste36mnd, 20000.årlig)
            besvar(inntektSiste12mnd, 5000.årlig)

            assertFalse(gjeldendeResultat())

            assertFalse(gjeldendeFakta("27.1")!!)
        }
    }

    //language=JSON
    private val søknadFraInnsending =
        """{
              "@event_name": "innsending_ferdigstilt",
              "fødselsnummer": "123456789",
              "aktørId": "",
              "søknadsId": "9876",
              "journalpostId": "493389306",
              "type": "NySøknad",
              "søknadsData": {
            "søknad_uuid": "10010WQMW"
          }
            }
        """.trimIndent()
}
