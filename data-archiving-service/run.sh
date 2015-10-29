#!/bin/sh
while ! curl -Is http://opentsdb:4242
do
  echo "$(date) - Failed to connect to OpenTSDB! Trying once more time..." 
  sleep 5;
done
echo "$(date) - Connected to OpenTSDB successfully"

java -jar target/$SERVICE_JAR_NAME.jar
