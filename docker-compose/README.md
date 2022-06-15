## Requirements 

- docker-compse 
- Personal github [token](https://docs.github.com/en/free-pro-team@latest/packages/using-github-packages-with-your-projects-ecosystem/configuring-docker-for-use-with-github-packages)

### Tldr;
1. Lag token
2. Sett token til feks miljøvariabel `GITHUB_TOKEN` (`export GITHUB_TOKEN=<token>`)
3. Logg inn i docker `echo $GITHUB_TOKEN | docker login ghcr.io -u <BRUKERNAVN> --password-stdin`

### Feilsøking:
- Hvis du får følgende feilmelding ved innlogging: ```Error saving credentials: error storing credentials - err: exit status 1, out:```

Så kan du løse det ved å gå til ```cd ~/.docker``` og fjerne ```config.json``` før du logger inn på nytt.



## How to run

``` docker-compose up -V ```

## How to shutdown 

``` docker-compose down  ```

		

