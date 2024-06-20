#!/usr/bin/env bash

echo 🚀 Klar ferdig gå!
SOKNAD_ID=$(uuidgen | tr "[:upper:]" "[:lower:]")
SCRIPT_PATH="$(dirname "$(readlink -f "$0")")"
echo $SOKNAD_ID > $SCRIPT_PATH/current_soknads_id
jq -nrc \
   --arg id  $(uuidgen | tr "[:upper:]" "[:lower:]")\
   --arg soknad_uuid $SOKNAD_ID \
   --arg opprettet $(date  +"%FT%T.%N") \
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