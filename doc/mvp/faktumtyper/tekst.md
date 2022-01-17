# Tekst

Kun aktuell for et faktum i MVP-en, som er faktumet for tilleggsinformasjon.
På lengre sikt kommer denne typen ikke til å være aktuell i Quiz. 

#### JSON-eksempel med svar
```
{
  "id": "9",
  "type": "tekst",
  "lengdeMax": 500,
  "beskrivendeId": "tekst9",
  "svar": "Dette er en tekst som kan inneholde linjeskift",
  "roller": [
    "søker"
  ]
}
```

##### Feltforklaringer

###### svar

Her må vi passe på sikkerheten ved at vi ikke får inn noe rusk. Sikkert lurt å sjekke for dette i flere ledd.

###### lengdeMax
Bruker samme maksimallengde som er brukt i den eksisterende søknadsløsningen.

#### UI skisser
