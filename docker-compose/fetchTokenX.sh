#!/bin/bash

if [ "$1" == "" ]; then
    IDENT=12345678901
    else
    IDENT=$1
fi

echo "Skal hente et lokalt TokenX-token for $IDENT fra fakedings"

curl "http://host.docker.internal:8080/fake/tokenx?client_id=someclientid&aud=localhost%3Ateamdagpenger%3Adp-soknad&acr=Level4&pid=$IDENT"
