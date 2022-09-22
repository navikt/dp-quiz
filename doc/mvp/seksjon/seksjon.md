# Seksjon

Seksjoner i Quiz er en logisk samling av [fakta](doc/mvp/faktumtyper/README.md).
Vi bruker det til å grupper fakta som kan besvares samtidig. En seksjon har roller etter hvem som skal besvare faktaene. 
Roller pt:

* NAV
  * representerer fakta som skal besvares i NAV. Det kan være register i NAV, som et eksempel, Innteksregisteret, Arbeidssøkeeregister, PDL mm  
* Søker
  *  representerer fakta som skal besvares av søker/sluttbruker i en dialog. 
* Saksbehandler
  *  representerer fakta som skal besvares av saksbehandler i NAV


## Rekkefølge i seksjon

Faktumrekkefølgen i en seksjon er determinert etter rekkefølgen faktum er instansiert i seksjonen. 

Gitt søknad og seksjoner: 

```kotlin
val søknad = Søknad(
            testversjon,
            dato faktum "f3" id 3,
            dato faktum "f4" id 4,
            dato faktum "f5" id 5,
            heltall faktum "f6" id 6,
            maks dato "maksdato" av 3 og 4 og 5 id 345
        )
val seksjon1 = søknad.seksjon("f6f3f4", Rolle.søker, 6, 3, 4)
val seksjon2 = søknad.seksjon("f345f5", Rolle.søker, 345, 5)
```

Vil `seksjon1` sin faktumrekkefølge være  `6, 3, 4` og
`seksjon2` sin faktumrekkefølge `345, 5`


Unntaket for `Rolle.søker` er: 
* Hvis et faktum spesfisert i seksjonen har avhengighet til et faktum i en annen seksjon vil de havne øverst. 

**_Felles for alle seksjoner:_**

* Reglene (subsumsjonene) bestemmer _om_ faktumene blir etterspurt
 