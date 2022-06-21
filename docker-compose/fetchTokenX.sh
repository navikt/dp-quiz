#!/bin/bash

if [ "$1" == "" ]; then
    IDENT=12345678901
    else
    IDENT=$1
fi

echo "Skal hente et token for $IDENT fra den lokale oauth-mock-serveren"

# Inntil videre må denne verdien manuelt oppdateres med innholdet i cookie-en `session-cookie` som er tilgjengelig på http://host.docker.internal:8080/tokenx/debugger
# TODO Hente ut denne verdien automatisk
DEBUGGER_SESSION_COOKIE="eyJlbmMiOiJBMTI4R0NNIiwiYWxnIjoiZGlyIn0..5q-DyM3Pmnnt9TF9.cUftjBUjC39aiBHb-6AND2S23AVEBOEpd0WVQDga--og7WKqPtHeO-SXEmSD1lPHratU7Dlr7LwJxWZMRT6IO1a5PgNrROA3-WzSvUe69FA-bGyN7Rk3hd90lqIqP5ubPhNh-_SJ48fCGe6bOnQossVvjt9QJ9474WpAKDlfc4OurZCQhDL34ILfxUlCHovdUlFHIdl7vdDzD0SwmX-qYSalMm9rMWAs1WLwTg2mkmTWAsiTfUiH_Uh11ygQitpMyZ0-J28NGblhBtIg7mTibj8sz0NhgsEhJ34ktDjaSJm1LchJUgih3EGKs17LjNe5FlGKLgi4WGiwXzhXfdOb6rgIu9nJc9EtpBJWsvwBi8NpAt0Iml59xzTW7MRx86aDhRxQyIRvOnoVqjckJQhtlJk1e21CTu-sNlMwX0rchgVTzO1XFWwEaIUm1xKBjrbyAapNOjnF3ySn-g3NN_400ZpevHa0TSVpt9ciDvc91JXgKzJ_gUvHvc0Y7BydwOlQV0tcx6nCJPqVARNJnpCP1Gu2lynOZPHd-PKXogUzp00gCY8tEgkJ5v4hEqPmCnVgmNL7-K0KomvV8CboGGfL8KKxjnMiQx2z._8VAqYsBDYDGRWCcPR-JmA"

curl -L --max-redirs 1 -s 'http://host.docker.internal:8080/tokenx/authorize?client_id=debugger&scope=openid+tokenx&response_type=code&response_mode=query&state=1234&nonce=5678&redirect_uri=http%3A%2F%2Fhost.docker.internal%3A8080%2Ftokenx%2Fdebugger%2Fcallback' \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  -H "Cookie: debugger-session=$DEBUGGER_SESSION_COOKIE" \
  -H 'Origin: http://host.docker.internal:8080' \
  -H 'Referer: http://host.docker.internal:8080/tokenx/authorize?client_id=debugger&scope=openid+tokenx&response_type=code&response_mode=query&state=1234&nonce=5678&redirect_uri=http%3A%2F%2Fhost.docker.internal%3A8080%2Ftokenx%2Fdebugger%2Fcallback' \
  --data-raw "username=$IDENT&claims=%7B%0D%0A%22pid%22%3A+%22$IDENT%22%0D%0A%7D" \
  --compressed \
  --insecure | grep "access_token"
