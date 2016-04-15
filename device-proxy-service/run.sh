#!/bin/sh

# Delete .lock files
rm -f /fuseki/fuseki-db/tdb.lock

if [ -e $FUSEKI_HOME/app/shiro.ini ]
then
        rm $FUSEKI_BASE/shiro.ini
        cp /fuseki/app/shiro.ini $FUSEKI_BASE/shiro.ini
fi

pushd $FUSEKI_HOME

./fuseki-server --config=base/config.ttl &> /root/semiot-platform/$SERVICE_NAME/proxy-logs/fuseki.log &

popd

while ! curl -Is http://localhost:3030
do
    echo "$(date) - Failed to connect to Fuseki! Trying once more time..."
    sleep 5;
done
echo "$(date) - Connected to Fuseki successfully"

exec java -jar -Dlogback.configurationFile=/root/semiot-platform/$SERVICE_NAME/logback.xml $SERVICE_JAR_NAME.jar >> /root/semiot-platform/$SERVICE_NAME/proxy-logs/deviceproxy.log 2>&1
