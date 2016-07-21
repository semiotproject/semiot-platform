FROM fedora:23

MAINTAINER SemIoT Project <semiotproject@gmail.com>

ENV SERVICE_NAME device-proxy-service
ENV SERVICE_JAR_NAME device-proxy-service-launcher-1.0-SNAPSHOT-jar-with-dependencies

WORKDIR /
RUN dnf install -y java-1.8.0-openjdk-headless nss && dnf clean all

WORKDIR /root/semiot-platform/$SERVICE_NAME

ADD device-proxy-service-launcher/target/$SERVICE_JAR_NAME.jar $SERVICE_JAR_NAME.jar
ADD logback.xml ./

#ENV JMX_OPTS -Dcom.sun.management.jmxremote.rmi.port=8989 -Djava.rmi.server.hostname=77.234.222.6 -Dcom.sun.management.jmxremote.port=8989 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.local.only=false

CMD java $JMX_OPTS -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/root/semiot-platform/$SERVICE_NAME/dump/dump.hprof -Dlogback.configurationFile=/root/semiot-platform/$SERVICE_NAME/logback.xml -jar $SERVICE_JAR_NAME.jar
