FROM fedora:23

MAINTAINER SemIoT Project <semiotproject@gmail.com>

ENV SERVICE_NAME device-proxy-service
ENV SERVICE_JAR_NAME device-proxy-service-launcher-1.0-SNAPSHOT-jar-with-dependencies

WORKDIR /
RUN dnf install -y java-1.8.0-openjdk-headless nss && dnf clean all

WORKDIR /root/semiot-platform/$SERVICE_NAME

ADD device-proxy-service-launcher/target/$SERVICE_JAR_NAME.jar $SERVICE_JAR_NAME.jar
ADD logback.xml ./

CMD java -Dcom.sun.management.jmxremote.rmi.port=8981 -Dcom.sun.management.jmxremote.port=8981 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.local.only=false -Dcom.sun.management.jmxremote.authenticate=$SERVICE_JMX_DISABLED -Djava.rmi.server.hostname=$SERVICE_JMX_DOMAIN -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/root/semiot-platform/$SERVICE_NAME/dump/dump.hprof -Dlogback.configurationFile=/root/semiot-platform/$SERVICE_NAME/logback.xml -jar $SERVICE_JAR_NAME.jar
