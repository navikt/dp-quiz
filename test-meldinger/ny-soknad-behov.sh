#!/bin/zsh

echo 🚀 Klar ferdig gå!
jq -nrc \
   --arg id  $(uuidgen | tr "[:upper:]" "[:lower:]")\
   --arg soknad_uuid $(uuidgen | tr "[:upper:]" "[:lower:]") \
   '{
       "@event_name": "behov",
       "@behov": [
         "NySøknad"
       ],
       "@opprettet": "2023-11-20",
       "@id": $id,
       "søknad_uuid": $soknad_uuid,
       "ident": "123456789",
       "prosessnavn": "Dagpenger"
     }' \
     | $HOME/kafka_2.13-3.6.0/bin/kafka-console-producer.sh --bootstrap-server localhost:9092 --topic teamdagpenger.rapid.v1
echo 🍾 Juuuuhuuuu!