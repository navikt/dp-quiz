# dp-quiz

QUIZ sitt formål:

Prosess for faktainnhenting og anvendelse av de faktaene på regler:

> Lage en prosess, styrt av regelverket, som kunne innhente/skaffe alle fakta som trengs for å ta en avgjørelse

Skal kunne:
> Lett kunne endre hvor fakta kommer fra

> Være endringsdyktig: regler og fakta

Sentrale begreper i QUIZ:

* Faktum - faktiske forhold som er av betydning i et saksforhold. Som for et eksempel "fødselsdato" (BEGREPSKATALOG - https://jira.adeo.no/browse/BEGREP-306)
  * [Faktumtyper i QUIZ](doc/mvp/faktumtyper/README.md)
* Subsumsjon - å anvende en rettsregel (lov) på et faktum. Som for et eksempel - søker må være under 67 år der "fødselsdato" anvendes på regelen. (BEGREPSKATALOG - https://jira.adeo.no/browse/BEGREP-817)
  * [Subsumsjonstyper i QUIZ](doc/mvp/subsumsjonstyper/README.md)
* Seksjoner - Seksjoner i Quiz er en logisk samling av [fakta](doc/mvp/faktumtyper/README.md).
  * [Seksjoner](doc/mvp/seksjon/seksjon.md)

## Komme i gang

Gradle brukes som byggverktøy og er bundlet inn!

`./gradlew build`

## Coding style

Vi bruker [`ktlint`](https://github.com/pinterest/ktlint) som linter og formatter for Kotlin.

### Konfigurere IntelliJ med ktlint sine regler

```
brew install ktlint
ktlint applyToIDEAProject
```

Bonus: Sette opp pre-commit hook:

```
ktlint installGitPreCommitHook
```

## Co-Authors

Siden vi praktiserer mye par- og mobprogrammering er det bra å legge på de man
jobber med som Co-Authors.

Installer [Co-Author](https://plugins.jetbrains.com/plugin/10952-co-author)
pluginen i IntelliJ.

Opprett en liste med commiters med:

```
git shortlog -es | cut -c8- > ~/.git_coauthors
```

# Henvendelser

Spørsmål knyttet til koden eller prosjektet kan rettes mot:

* André Roaldseth, andre.roaldseth@nav.no
* Eller en annen måte for omverden å kontakte teamet på

## For NAV-ansatte

Interne henvendelser kan sendes via Slack i kanalen #dagpenger-dev


# HOWTOS

## Hente faktumverdier gitt en søknad

```postgresql

WITH soknad_faktum AS (SELECT faktum.id as faktum_id, faktum.root_id AS root_id, soknad.id AS soknad_id
                       FROM soknad,
                            faktum
                       WHERE faktum.versjon_id = soknad.versjon_id
                         AND faktum.regel IS NULL
                         AND soknad.uuid = '<søknad_uuid>'::uuid)
SELECT soknad_faktum.root_id       as root_id,
       faktum_verdi.faktum_id      as faktum_id,
       n.navn                      as navn,
       faktum_verdi.indeks         as indeks,
       faktum_verdi.heltall        AS heltall,
       faktum_verdi.desimaltall    AS desimaltall,
       faktum_verdi.boolsk         AS boolsk,
       faktum_verdi.dato           AS dato,
       faktum_verdi.aarlig_inntekt AS aarlig_inntekt,
       besvarer.identifikator      AS besvartAv,
       dokument.url                AS url,
       dokument.opplastet          AS opplastet
FROM faktum_verdi
         JOIN soknad_faktum ON faktum_verdi.soknad_id = soknad_faktum.soknad_id
    AND faktum_verdi.faktum_id = soknad_faktum.faktum_id
         LEFT JOIN dokument ON faktum_verdi.dokument_id = dokument.id

         LEFT JOIN besvarer ON faktum_verdi.besvart_av = besvarer.id
         LEFT JOIN navn n on faktum_verdi.faktum_id = n.id
ORDER BY indeks;
```

## Oppgradering av gradle wrapper
Finn nyeste versjon av gradle her: https://gradle.org/releases/
Kjør så følgende kommando:
```./gradlew wrapper --gradle-version $gradleVersjon```
