#!/bin/bash

if [ "$1" == "" ]; then
    IDENT=12345678901
    else
    IDENT=$1
fi

echo "Skal hente et lokalt IDPorten-token for $IDENT fra fakedings"

curl "http://host.docker.internal:8080/fake/idporten?pid=$IDENT&acr=Level4"
