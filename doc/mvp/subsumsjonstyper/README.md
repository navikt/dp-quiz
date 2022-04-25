# Subsumsjon

## Definisjon

[Subsumsjon (av latin) betyr å anvende en rettsregel (lov) på et faktum.](https://jusleksikon.no/wiki/Subsumsjon)

I Quiz er det ikke begrenset til at regel må være en _rettsregel_ , det kan være prosessregel, flytregel etc.  

## Subsumsjonstyper i Quiz

### Enkel subsumsjon (EnkelSubsumsjon)

Det laveste nivået - der en regel anvendes på et eller flere faktum.

Eksempel;

Faktum
```kotlin
 boolsk faktum "Har mulighet til å jobbe heltid og deltid" id kanJobbeDeltid avhengerAv innsendtSøknadsId
```
Subsumsjonen
```kotlin
 boolsk(kanJobbeDeltid) er true
```

### Sammensatte subsumsjon (SammenSattSubsumsjon)

Av navnet, subsumsjoner som på forskjellige måter delegerer til andre subsumsjoner. 

#### AlleSubsumsjon 

En subsumsjonstype der _alle_ subsumsjonene definert må være sanne.

Eksempel:

```kotlin
"er reell arbeidssøker".alle(
            boolsk(kanJobbeDeltid) er true,
            boolsk(helseTilAlleTyperJobb) er true,
            boolsk(kanJobbeHvorSomHelst) er true,
            boolsk(villigTilÅBytteYrke) er true
        )
```

#### MinstEnAvSubsumsjon 

En subsumsjonstype der _minst en av_ subsumsjonene definert må være sanne.

Eksempel:

```kotlin
"minste arbeidsinntekt".minstEnAv(
            inntekt(inntektSiste36mnd) minst inntekt(minsteinntektsterskel36mnd),
            inntekt(inntektSiste12mnd) minst inntekt(minsteinntektsterskel12mnd),
            boolsk(verneplikt) er true,
        )

```

#### BareEnAvSubsumsjon

En subsumsjonstype der _bare en av_ subsumsjonene definert må være sanne.

```kotlin
 "Enten joda eller neida".bareEnAv(
        neida er true,
        joda er true,
        ja1 er true
    )
```


#### DeltreSubsumsjon

En subsumsjonstype som delegerer evalueringen til underliggende subsumsjons-tre

```kotlin
generator(registrertArbeidssøkerPerioder) har "arbeidsøkerregistrering".deltre {
            dato(førsteAvVirkningsdatoOgBehandlingsdato) mellom
                dato(registrertArbeidssøkerPeriodeFom) og dato(registrertArbeidssøkerPeriodeTom)
        }
```

#### GodkjenningsSubsumsjon 

En subsumsjonstype som brukes til å "godkjenne" en annen subsumsjon. I praksis vil en godkjenne fakta brukt i subsumsjonen en skal godkjenne. Det er flere strategier som kan brukes til å overstyre subsumsjon. 


