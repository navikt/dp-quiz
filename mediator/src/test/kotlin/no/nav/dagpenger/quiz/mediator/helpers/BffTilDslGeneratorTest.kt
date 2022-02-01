package no.nav.dagpenger.quiz.mediator.helpers

import org.junit.jupiter.api.Test

class BffTilDslGeneratorTest {

    @Test
    fun `Skal kunne konvertere fra BFF-json-seksjoner til Quiz-DSL`() {
        val generator = BffTilDslGenerator(dummySeksjonJSON)
        println(generator)
    }
}

//language=JSON
val dummySeksjonJSON =
    """{
  "id": "dummy-seksjon-data",
  "faktum": [
    {
      "id": "faktum.dummy-boolean",
      "type": "boolean",
      "answerOptions": [
        { "id": "faktum.dummy-boolean.svar.ja" },
        { "id": "faktum.dummy-boolean.svar.nei" }
      ]
    },
    {
      "id": "faktum.dummy-valg",
      "type": "valg",
      "answerOptions": [
        { "id": "faktum.dummy-valg.svar.ja" },
        { "id": "faktum.dummy-valg.svar.nei" },
        { "id": "faktum.dummy-valg.svar.vetikke" }
      ],
      "subFaktum": [
        {
          "id": "faktum.dummy-subfaktum-tekst",
          "type": "tekst",
          "requiredAnswerId": ["faktum.dummy-valg.svar.ja"]
        }
      ]
    },
    {
      "id": "faktum.dummy-flervalg",
      "type": "flervalg",
      "answerOptions": [
        { "id": "faktum.dummy-flervalg.svar.1" },
        { "id": "faktum.dummy-flervalg.svar.2" },
        { "id": "faktum.dummy-flervalg.svar.3" }
      ]
    },
    {
      "id": "faktum.dummy-dropdown",
      "type": "dropdown",
      "answerOptions": [
        { "id": "faktum.dummy-dropdown.svar.1" },
        { "id": "faktum.dummy-dropdown.svar.2" },
        { "id": "faktum.dummy-dropdown.svar.3" }
      ]
    },
    {
      "id": "faktum.dummy-int",
      "type": "int"
    },
    {
      "id": "faktum.dummy-double",
      "type": "double"
    },
    {
      "id": "faktum.dummy-tekst",
      "type": "tekst"
    },
    {
      "id": "faktum.dummy-localdate",
      "type": "localdate"
    },
    {
      "id": "faktum.dummy-periode",
      "type": "periode"
    },
    {
      "id": "faktum.dummy-generator",
      "type": "generator",
      "faktum": [
        {
          "id": "faktum.generator-dummy-boolean",
          "type": "boolean",
          "answerOptions": [
            { "id": "faktum.generator-dummy-boolean.svar.ja" },
            { "id": "faktum.generator-dummy-boolean.svar.nei" }
          ]
        },
        {
          "id": "faktum.generator-dummy-valg",
          "type": "valg",
          "answerOptions": [
            { "id": "faktum.generator-dummy-valg.svar.ja" },
            { "id": "faktum.generator-dummy-valg.svar.nei" },
            { "id": "faktum.generator-dummy-valg.svar.vetikke" }
          ],
          "subFaktum": [
            {
              "id": "faktum.generator-dummy-subfaktum-tekst",
              "type": "tekst",
              "requiredAnswerId": ["faktum.generator-dummy-valg.svar.ja"]
            }
          ]
        },
        {
          "id": "faktum.generator-dummy-flervalg",
          "type": "flervalg",
          "answerOptions": [
            { "id": "faktum.generator-dummy-flervalg.svar.1" },
            { "id": "faktum.generator-dummy-flervalg.svar.2" },
            { "id": "faktum.generator-dummy-flervalg.svar.3" }
          ]
        },
        {
          "id": "faktum.generator-dummy-dropdown",
          "type": "dropdown",
          "answerOptions": [
            { "id": "faktum.generator-dummy-dropdown.svar.1" },
            { "id": "faktum.generator-dummy-dropdown.svar.2" },
            { "id": "faktum.generator-dummy-dropdown.svar.3" }
          ]
        },
        {
          "id": "faktum.generator-dummy-int",
          "type": "int"
        },
        {
          "id": "faktum.generator-dummy-double",
          "type": "double"
        },
        {
          "id": "faktum.generator-dummy-tekst",
          "type": "tekst"
        },
        {
          "id": "faktum.generator-dummy-localdate",
          "type": "localdate"
        },
        {
          "id": "faktum.generator-dummy-periode",
          "type": "periode"
        }
      ]
    }
  ]
}"""
