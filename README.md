# SemIoT Platform

## How to deploy

First of all, you need to install Docker and Docker Compose, if you didn't do it yet. Read [instructions](https://docs.docker.com/compose/install/).

Then you're ready to pull the images from Docker Hub (it takes several minutes):
```
sudo docker-compose pull
```
Place [opentsdb.conf](https://github.com/semiotproject/semiot-platform/blob/master/data-archiving-service/opentsdb.conf) file to `/etc/opentsdb` folder. Start up the containers:
```
sudo docker-compose up -d
```

If you want to see the logs of the containers then run:
```
sudo docker-compose logs
```

## How to add a device driver

OSGI-WebConsole is available on `http://${HOST}:${PORT}/8181/sustem/console`.

During first start, you must configure Device Proxy Service Manager. Open WebConsole and select menu item `OSGI` > `Configuration` > `Device Proxy Service Manager`. Set next default parameters: 
* WAMP URI - `ws://wamprouter:8080/ws`
* WAMP Realm - `realm1`
* WAMP Reconnect interval - `15`
* Topic register - `ru.semiot.devices.register`
* Topic new and observing - `ru.semiot.devices.newandobserving`

![Console Configuration] (https://raw.githubusercontent.com/semiotproject/semiot-platform/master/images/Console%20Configuration.png)

![Configuration Device Proxy Service Maneger] (https://raw.githubusercontent.com/semiotproject/semiot-platform/master/images/Configuration%20Device%20Proxy%20Service%20Maneger.png?raw=true)

Next, you must instal your bundle and set it's configuration. More detail on [apache-felix-web-console](http://felix.apache.org/documentation/subprojects/apache-felix-web-console.html).

TODO

For example, you can create a bundles of [existing drivers](https://github.com/semiotproject/semiot-platform/tree/master/device-proxy-service-drivers).

