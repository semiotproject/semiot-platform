FROM fedora:23

MAINTAINER Shilin Ivan <proniment@gmail.com>

ENV SERVICE_NAME device-proxy-service
ENV SERVICE_JAR_NAME device-proxy-service-launcher-1.0-SNAPSHOT-jar-with-dependencies

WORKDIR /
RUN dnf install -y java-1.8.0-openjdk-headless nss-3.23.0 && dnf clean all

WORKDIR /root/semiot-platform/$SERVICE_NAME

ADD device-proxy-service-launcher/target/$SERVICE_JAR_NAME.jar $SERVICE_JAR_NAME.jar
ADD logback.xml ./

CMD java -jar -Dlogback.configurationFile=/root/semiot-platform/$SERVICE_NAME/logback.xml $SERVICE_JAR_NAME.jar
