#!/usr/bin/env bash

HOST=${1:-cassandra}
USER=${2:-cassandra}
PASSWORD=${3:-cassandra}
CQL=${4:-schema.cql}

until cqlsh ${HOST} -u ${USER} -p ${PASSWORD} -f ${CQL}; do
  echo "Cassandra is unavailable - will retry..."
  sleep 5
done

sleep 1
echo "...Done Cassandra schema"