#!/bin/zsh

echo Kontrollerer at homebrew er installert...
command -v brew >/dev/null 2>&1 ||  (echo >&2 "Du må installere homebrew. Se https://docs.brew.sh/Installation." && exit 1);
echo ✅ homebrew

echo Kontrollerer at jq er installert...
command -v jq >/dev/null 2>&1 || (echo >&2 "jq var ikke installert. Installerer jq..." && brew install jq);
echo ✅ jq

if [ ! -f "$HOME/kafka_2.13-3.6.0/bin/kafka-console-producer.sh" ]; then
    echo Finner ikke Kafka CLI. Installerer Kafka CLI...
    curl https://downloads.apache.org/kafka/3.6.0/kafka_2.13-3.6.0.tgz --output /tmp/kafka.tgz
    tar xzvf /tmp/kafka.tgz -C ~/
    rm /tmp/kafka.tgz
fi

echo Det burde alt være i orden. Her er en traktor 🚜!