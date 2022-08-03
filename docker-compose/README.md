# Quiz på boks
Muliggjøre lokal kjøring av søknadsdialogen og quiz lokalt.

## Forutsettninger 
* Nødvendig programvare, kan legges inn vha Brew: 
  * Colima
  * docker-compose 
* Github Personal Access Token (PAT) [token](https://docs.github.com/en/free-pro-team@latest/packages/using-github-packages-with-your-projects-ecosystem/configuring-docker-for-use-with-github-packages)
* `127.0.0.1 host.docker.internal` må være lagt til i `/etc/hosts`

### Opprette PAT hos Github
1. Lag et PAT hos Github, må gjøres fr å kunne laste ned docker-images fra Github sitt pakkerepo.
   1. `Github.com > Settings > Developer settings > Personal access tokens > Generate new token` ([direkte lenke](https://github.com/settings/tokens))
   2. Sørg for at å krysse av for tilgang til å lese pakker.
   3. Etter at tokenet er lagd er det viktig å klikke på "Configure SSO" og velge "Authorize", og deretter følge flyten.
2. Sett tokenet til f.eks. miljøvariabel `GITHUB_TOKEN` (`export GITHUB_TOKEN=<token>`)
3. Logg inn i docker `echo $GITHUB_TOKEN | docker login ghcr.io -u <BRUKERNAVN> --password-stdin`


### Bruke søknadsdialogen lokalt
1. `docker-compose up -d` --> starter alle containerene i bakgrunnen.
2. Gå til http://localhost:4000/, og fyll inn et fødselsnummer.
3. Fyll inn det samme fødselsnummeret i claim-et pid:
```json
{
  "pid": "<samme fødselsnummer som i steg 2>"
}
```
4. Klikk på Sign-in.

NB! Det er opprettet en egen [oppgave](https://jira.adeo.no/browse/DAG-340) på å automatisere steg 2-4.

### Stoppe alle containere
* `docker-compose down` --> stopper alle kjørende containere, men beholder tilstand i databaser og på kafka. 


### Feilsøking
* Verifisere at alle containere kjører: `docker-compose ps`
* Tail-e loggene for alle containerene: `docker-compose logs -f`
* Hvis oppsettet ikke oppfører seg som forventet kan følgende kommandø kjøres `docker-compose down -v`, det sørger for å
  rydde opp etter tidligere kjøringer.
