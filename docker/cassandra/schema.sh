#!/usr/bin/env bash

HOST=${1:-cassandra}
USER=${2:-cassandra}
PASSWORD=${3:-cassandra}
KEYSPACE=${4:-mykeyspace}

# Create keyspace for single node cluster
KEYSPACE_CQL="CREATE KEYSPACE IF NOT EXISTS $KEYSPACE WITH REPLICATION = {'class': 'SimpleStrategy', 'replication_factor': 1};"

until echo ${KEYSPACE_CQL} | cqlsh ${HOST} -u ${USER} -p ${PASSWORD}; do
  echo "cqlsh: Cassandra is unavailable - will retry..."
  sleep 3
done

echo "...Done Cassandra schema"





#HOST=${1:-mongo}
#function configure {
#  declare -a arr=("test" "accounts" "crmg" "trending" "customer" "customer_passes" "popcorn" "schedule" "users"
#    "ingestor" "content_id_mapping" "products" "gifting" "viewing" "pins_cache"  "playout_report" "stubs"
#    "pins_migration" "view_history" "request_cache" "migration")
#  mongo --host "$HOST" --eval "db.system.users.remove({})"
#  mongo --host "$HOST" --eval "db.createUser({ user: 'admin', pwd: 'kernel', roles: [{ role: 'userAdminAnyDatabase', db: 'admin' }], mechanisms: ['SCRAM-SHA-1'] })"
#  for (( i=0; i<${#arr[@]}; i++ )); do
#    mongo "$HOST"/"${arr[i]}" --eval "db.createUser({ user: 'popcorn', pwd: 'kernel', roles: [{ role: 'readWrite', db: '${arr[i]}' }], mechanisms: ['SCRAM-SHA-1'] })"
#  done
#  mongo "$HOST"/customer --eval "db.accounts.ensureIndex({ 'trackingIds.player': 1 }, { 'name': 'trackingIds.player' })"
#  mongo "$HOST"/users --eval "db.fs.chunks.ensureIndex({ 'files_id': 1, n: 1 })"
#  mongo "$HOST"/popcorn --eval "db.genres.ensureIndex({ 'genres._id': 1 })"
#  mongo "$HOST"/popcorn --eval "db.programs.ensureIndex({ 'programs.licence': 1 })"
#  mongo "$HOST"/popcorn --eval "db.helpArticle.ensureIndex({ 'helpArticle.type': 1 })"
#  mongo "$HOST"/popcorn --eval "db.helpArticle.ensureIndex({ 'helpArticle.devices': 1 })"
#  mongo "$HOST"/popcorn --eval "db.helpArticle.ensureIndex({ 'helpArticle.clients': 1 })"
#  mongo "$HOST"/popcorn --eval "db.passes.ensureIndex({ 'consumption.start.date': 1 })"
#}
#until mongo --host mongo --eval "print('Waiting for Mongo connection...')"
#do
#    sleep 1
#done
#echo "Mongo configuration..."
#configure
#echo "...Done Mongo configuration"