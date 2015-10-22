#!/bin/sh
while ! curl -Is http://opentsdb:4242
do
  sleep 1;
done
echo "$(date) - connected successfully"

java -jar target/$SERVICE_JAR_NAME.jar