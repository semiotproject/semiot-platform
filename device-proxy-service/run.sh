#!/bin/sh

# Delete .lock files
rm -f /fuseki/fuseki-db/tdb.lock

pushd $FUSEKI_HOME

./fuseki-server --config=base/config.ttl &> fuseki.log &

popd

while ! curl -Is http://localhost:3030
do
    echo "$(date) - Failed to connect to Fuseki! Trying once more time..."   
    sleep 5;
done
echo "$(date) - Connected to Fuseki successfully"

java -jar $SERVICE_JAR_NAME.jar
