#!/bin/sh
while ! curl http://demo.semiot.ru:3030
do
  sleep 1;
done
echo "$(date) - connected successfully"

java -jar target/$SERVICE_JAR_NAME.jar