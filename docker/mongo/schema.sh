#!/usr/bin/env bash

HOST=${1:-mongo}

function schema {
  mongo --host ${HOST} << EOF
    use mydatabase
    db.createCollection("users")
EOF
}

until mongo --host ${HOST} --eval "print('Waiting for Mongo connection...')"; do
  sleep 5
done

schema
sleep 1
echo "...Done Mongo configuration"