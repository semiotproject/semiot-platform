FROM fedora:23

MAINTAINER Shilin Ivan <proniment@gmail.com>

ENV SERVICE_NAME data-archiving-service
ENV SERVICE_JAR_NAME data-archiving-service-1.0-SNAPSHOT-jar-with-dependencies

RUN dnf install -y java-1.8.0-openjdk-headless nss && dnf clean all

WORKDIR /root/semiot-platform/$SERVICE_NAME

ADD target/$SERVICE_JAR_NAME.jar $SERVICE_JAR_NAME.jar

ADD run.sh ./
RUN chmod 777 run.sh

CMD ./run.sh
