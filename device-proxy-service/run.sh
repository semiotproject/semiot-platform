#!/bin/sh
while ! curl -Is http://devicedirectoryservice:3030
do
  sleep 1;
done
echo "$(date) - connected successfully"

java -jar device-proxy-service-launcher/target/$SERVICE_JAR_NAME.jar