FROM fedora:22

MAINTAINER Shilin Ivan <proniment@gmail.com>

ENV SERVICE_NAME device-proxy-service
ENV SERVICE_JAR_NAME device-proxy-service-launcher-1.0-SNAPSHOT-jar-with-dependencies

RUN dnf upgrade -y && dnf install -y java-1.8.0-openjdk-headless && dnf clean all

WORKDIR /root/semiot-platform/$SERVICE_NAME

ADD device-proxy-service-launcher/target/$SERVICE_JAR_NAME.jar $SERVICE_JAR_NAME.jar

ADD run.sh ./
RUN chmod 777 run.sh

CMD ./run.sh
