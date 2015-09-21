#!/bin/bash

java -jar target/$SERVICE_JAR_NAME.jar &> devicedirectory.log &

pushd $FUSEKI_HOME

./fuseki-server --config=base/config.ttl

popd
