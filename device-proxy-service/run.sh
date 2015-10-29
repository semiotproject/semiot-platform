#!/bin/sh
while ! curl -Is http://devicedirectoryservice:3030
do
    echo "$(date) - Failed to connect to Fuseki! Trying once more time..."   
    sleep 5;
done
echo "$(date) - Connected to Fuseki successfully"

java -jar $SERVICE_JAR_NAME.jar
