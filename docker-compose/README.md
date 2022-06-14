# Requirements 

- docker-compse 
- Personal github [token](https://docs.github.com/en/free-pro-team@latest/packages/using-github-packages-with-your-projects-ecosystem/configuring-docker-for-use-with-github-packages)

tldr;
1. Lag token
2. Sett token til feks miljøvariabel `GITHUB_TOKEN` (`export GITHUB_TOKEN=<token>`)
3. Logg inn i docker `echo $GITHUB_TOKEN | docker login ghcr.io -u <BRUKERNAVN> --password-stdin`

# How to run

``` docker-compose up -V ```

# How to shutdown 

``` docker-compose down  ```

		

