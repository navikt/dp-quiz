# Generator

Faktumtype som typisk representerer lister, typisk over barn eller arbeidsforhold. Selve svaret på faktumet kan trigge 
at det må svares på flere fakta. F.eks. hvis man har flere barn enn det som kom preutfylt fra Quiz, så vil det blir 
genererte fakta for å svare på info om det barnet som manglet. Se på [dette eksempelet](#forklaring-av-eksempelet) for 
mer detaljer.

#### JSON-eksempel med svar
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
      "type": "tekst"
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
  ],
  "roller": [
    "søker"
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
