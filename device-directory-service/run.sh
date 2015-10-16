#!/bin/bash

# Delete .lock files
rm -f /fuseki/fuseki-db/tdb.lock

pushd $FUSEKI_HOME

./fuseki-server --config=base/config.ttl &> fuseki.log &

popd

java -jar target/$SERVICE_JAR_NAME.jar
