package no.nav.dagpenger.quiz.mediator.helpers

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class BffTilDslGeneratorTest {

    @Test
    fun `Skal kunne konvertere fra BFF-json-seksjoner til Quiz-DSL`() {
        val fileAsJson = `dummy-seksjon-ts`.fjernTypescriptSyntax()

        val dsl = BffTilDslGenerator(fileAsJson)

        assertEquals(forventetDsl, dsl.dslResultat())
        assertEquals(forventedeVariabler, dsl.variabler())
    }
}

private val forventedeVariabler =
    """
        const val `dummy-boolean` = 1
        const val `dummy-valg` = 2
        const val `dummy-subfaktum-tekst` = 3
        const val `dummy-flervalg` = 4
        const val `dummy-dropdown` = 5
        const val `dummy-int` = 6
        const val `dummy-double` = 7
        const val `dummy-tekst` = 8
        const val `dummy-localdate` = 9
        const val `dummy-periode` = 10
        const val `dummy-generator` = 11
        const val `generator-dummy-boolean` = 12
        const val `generator-dummy-valg` = 13
        const val `generator-dummy-subfaktum-tekst` = 14
        const val `generator-dummy-flervalg` = 15
        const val `generator-dummy-dropdown` = 16
        const val `generator-dummy-int` = 17
        const val `generator-dummy-double` = 18
        const val `generator-dummy-tekst` = 19
        const val `generator-dummy-localdate` = 20
        const val `generator-dummy-periode` = 21
    """.trimIndent()

private val forventetDsl =
    """
boolsk faktum "faktum.dummy-boolean" id `dummy-boolean`,
envalg faktum "faktum.dummy-valg"
  med "svar.ja"
  med "svar.nei"
  med "svar.vetikke" id `dummy-valg`,
tekst faktum "faktum.dummy-subfaktum-tekst" id `dummy-subfaktum-tekst`,
flervalg faktum "faktum.dummy-flervalg"
  med "svar.1"
  med "svar.2"
  med "svar.3" id `dummy-flervalg`,
envalg faktum "faktum.dummy-dropdown"
  med "svar.1"
  med "svar.2"
  med "svar.3" id `dummy-dropdown`,
heltall faktum "faktum.dummy-int" id `dummy-int`,
desimaltall faktum "faktum.dummy-double" id `dummy-double`,
tekst faktum "faktum.dummy-tekst" id `dummy-tekst`,
dato faktum "faktum.dummy-localdate" id `dummy-localdate`,
periode faktum "faktum.dummy-periode" id `dummy-periode`,
heltall faktum "faktum.dummy-generator" id `dummy-generator`
  genererer `generator-dummy-boolean`
  og `generator-dummy-valg`
  og `generator-dummy-flervalg`
  og `generator-dummy-dropdown`
  og `generator-dummy-int`
  og `generator-dummy-double`
  og `generator-dummy-tekst`
  og `generator-dummy-localdate`
  og `generator-dummy-periode`,
boolsk faktum "faktum.generator-dummy-boolean" id `generator-dummy-boolean`,
envalg faktum "faktum.generator-dummy-valg"
  med "svar.ja"
  med "svar.nei"
  med "svar.vetikke" id `generator-dummy-valg`,
tekst faktum "faktum.generator-dummy-subfaktum-tekst" id `generator-dummy-subfaktum-tekst`,
flervalg faktum "faktum.generator-dummy-flervalg"
  med "svar.1"
  med "svar.2"
  med "svar.3" id `generator-dummy-flervalg`,
envalg faktum "faktum.generator-dummy-dropdown"
  med "svar.1"
  med "svar.2"
  med "svar.3" id `generator-dummy-dropdown`,
heltall faktum "faktum.generator-dummy-int" id `generator-dummy-int`,
desimaltall faktum "faktum.generator-dummy-double" id `generator-dummy-double`,
tekst faktum "faktum.generator-dummy-tekst" id `generator-dummy-tekst`,
dato faktum "faktum.generator-dummy-localdate" id `generator-dummy-localdate`,
periode faktum "faktum.generator-dummy-periode" id `generator-dummy-periode`
""".trim()

private const val `dummy-seksjon-ts` =
    """
import { MockDataSeksjon } from "./soknad";

export const dummySeksjon: MockDataSeksjon = {
  id: "dummy-seksjon-data",
  faktum: [
    {
      id: "faktum.dummy-boolean",
      type: "boolean",
      answerOptions: [
        { id: "faktum.dummy-boolean.svar.ja" },
        { id: "faktum.dummy-boolean.svar.nei" },
      ],
    },
    {
      id: "faktum.dummy-valg",
      type: "valg",
      answerOptions: [
        { id: "faktum.dummy-valg.svar.ja" },
        { id: "faktum.dummy-valg.svar.nei" },
        { id: "faktum.dummy-valg.svar.vetikke" },
      ],
      subFaktum: [
        {
          id: "faktum.dummy-subfaktum-tekst",
          type: "tekst",
          requiredAnswerIds: ["faktum.dummy-valg.svar.ja"],
        },
      ],
    },
    {
      id: "faktum.dummy-flervalg",
      type: "flervalg",
      answerOptions: [
        { id: "faktum.dummy-flervalg.svar.1" },
        { id: "faktum.dummy-flervalg.svar.2" },
        { id: "faktum.dummy-flervalg.svar.3" },
      ],
    },
    {
      id: "faktum.dummy-dropdown",
      type: "dropdown",
      answerOptions: [
        { id: "faktum.dummy-dropdown.svar.1" },
        { id: "faktum.dummy-dropdown.svar.2" },
        { id: "faktum.dummy-dropdown.svar.3" },
      ],
    },
    {
      id: "faktum.dummy-int",
      type: "int",
    },
    {
      id: "faktum.dummy-double",
      type: "double",
    },
    {
      id: "faktum.dummy-tekst",
      type: "tekst",
    },
    {
      id: "faktum.dummy-localdate",
      type: "localdate",
    },
    {
      id: "faktum.dummy-periode",
      type: "periode",
    },
    {
      id: "faktum.dummy-generator",
      type: "generator",
      faktum: [
        {
          id: "faktum.generator-dummy-boolean",
          type: "boolean",
          answerOptions: [
            { id: "faktum.generator-dummy-boolean.svar.ja" },
            { id: "faktum.generator-dummy-boolean.svar.nei" },
          ],
        },
        {
          id: "faktum.generator-dummy-valg",
          type: "valg",
          answerOptions: [
            { id: "faktum.generator-dummy-valg.svar.ja" },
            { id: "faktum.generator-dummy-valg.svar.nei" },
            { id: "faktum.generator-dummy-valg.svar.vetikke" },
          ],
          subFaktum: [
            {
              id: "faktum.generator-dummy-subfaktum-tekst",
              type: "tekst",
              requiredAnswerIds: ["faktum.generator-dummy-valg.svar.ja"],
            },
          ],
        },
        {
          id: "faktum.generator-dummy-flervalg",
          type: "flervalg",
          answerOptions: [
            { id: "faktum.generator-dummy-flervalg.svar.1" },
            { id: "faktum.generator-dummy-flervalg.svar.2" },
            { id: "faktum.generator-dummy-flervalg.svar.3" },
          ],
        },
        {
          id: "faktum.generator-dummy-dropdown",
          type: "dropdown",
          answerOptions: [
            { id: "faktum.generator-dummy-dropdown.svar.1" },
            { id: "faktum.generator-dummy-dropdown.svar.2" },
            { id: "faktum.generator-dummy-dropdown.svar.3" },
          ],
        },
        {
          id: "faktum.generator-dummy-int",
          type: "int",
        },
        {
          id: "faktum.generator-dummy-double",
          type: "double",
        },
        {
          id: "faktum.generator-dummy-tekst",
          type: "tekst",
        },
        {
          id: "faktum.generator-dummy-localdate",
          type: "localdate",
        },
        {
          id: "faktum.generator-dummy-periode",
          type: "periode",
        },
      ],
    },
  ],
};
"""
