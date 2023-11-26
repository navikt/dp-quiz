#!/usr/bin/env bash

echo 🚀 Klar ferdig gå!
SCRIPT_PATH="$(dirname "$(readlink -f "$0")")"
SOKNAD_ID=$(cat $SCRIPT_PATH/current_soknads_id)

jq -nrc \
   --arg id  $(uuidgen | tr "[:upper:]" "[:lower:]")\
   --arg soknad_uuid $SOKNAD_ID \
   --arg opprettet $(date  +"%FT%T.%N") \
   '{
      "@event_name": "faktum_svar",
      "@opprettet": $opprettet,
      "@id": $id,
      "besvart": $opprettet,
      "fakta": [
        {
          "id": "6001",
          "type": "land",
          "svar": "NOR"
        }
      ],
      "søknad_uuid": $soknad_uuid
    }' \
     | $HOME/kafka_2.13-3.6.0/bin/kafka-console-producer.sh --bootstrap-server localhost:9092 --topic teamdagpenger.rapid.v1
echo 🍾 Juuuuhuuuu!