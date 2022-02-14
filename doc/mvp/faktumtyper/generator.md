# Generator

Faktumtype som typisk representerer lister, typisk over barn eller arbeidsforhold. Selve svaret på faktumet kan trigge 
at det må svares på flere fakta. F.eks. hvis man har flere barn enn det som kom preutfylt fra Quiz, så vil det blir 
genererte fakta for å svare på info om det barnet som manglet. Se på [dette eksempelet](#forklaring-av-eksempelet) for 
mer detaljer.

### Eksempel på svarflyt for generatorfaktum

#### 1. Generatorfaktum uten svar
Etter at frontend har spurt Quiz om hvilke fakta som skal besvares, vil det se slik ut for et generatorfaktum:
```
{
  "id": "10",
  "type": "generator",
  "beskrivendeId": "antallBarn",
  "templates": [
    {
      "id": "11",
      "beskrivendeId": "navn",
      "type": "tekst"
    },
    {
      "id": "12",
      "beskrivendeId": "fødselsnummer",
      "type": "dato"
    }
  ],
  "svar": [],
  "roller": [
    "søker"
  ]
}
```

#### 2. Besvarelse på generatorfaktum
Dette er formatet frontenden svarer på et generatorfaktum:
 
NB!
Legg merke til svarformatet: Svarliste med et innslag per barn
For hvert barn er det en liste med faktumsvar som matcher generatorfaktumet sin template og ider.

```
{
  "fakta": [
    {
      "id": "10",
      "svar": [
        [
          {
            "id": "11",
            "svar": "Ola Nordmann",
            "type": "tekst"
          },
          {
            "id": "12",
            "svar": "2010-01-08",
            "type": "dato"
          },
        ],
        [
          {
            "id": "11",
            "svar": "Kari Nordmann",
            "type": "tekst"
          },
          {
            "id": "12",
            "svar": "2015-04-16",
            "type": "dato"
          },
        ]
      ],
      "type": "generator"
    }
  ]
}
```

#### 3. Generatorfaktum med svar
Etter at Quiz har prossesert besvarelsen vil den pånytt sende ut generatorfaktumet, men nå med svar.
```
{
  "id": "10",
  "type": "generator",
  "beskrivendeId": "antallBarn",
  "templates": [
    {
      "id": "11",
      "beskrivendeId": "navn",
      "type": "tekst"
    },
    {
      "id": "12",
      "beskrivendeId": "fødselsnummer",
      "type": "dato"
    }
  ],
  "svar": [
      {
        "navn": "Ola Nordmann",
        "fødselsnummer": "2010-01-08"
      },
      {
        "navn": "Kari Nordmann",
        "fødselsnummer": "2015-04-16"
      }
  ]
}
```

##### Feltforklaringer

###### templates

Mal som inneholder de fakta det må svares for hvis det skal legges til et barn til.


##### Forklaring av eksempelet
I dette eksempelet er svaret på generatorfaktumet to, og det vil dermed være to innslag i listen i svaret. Det er to 
barn, og det må svares på de samme fakta for begge barna. Feltet `templates` beskriver hvilke fakta som må besvares per 
barn.

Hvis det skal legges til et barn, mao at svaret på generatorfaktumet er tre. Så vil feltet `templates` beskrive hvilke 
ytterligere fakta som må besvares. Her blir dette navn og fødselsnummer.

#### UI skisser
