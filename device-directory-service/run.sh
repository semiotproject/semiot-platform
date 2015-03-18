#!/bin/bash

$FUSEKI_HOME/fuseki-server --mem --update /ds &> $FUSEKI_HOME/fuseki.log &

java -jar target/$SERVICE_JAR_NAME.jar
