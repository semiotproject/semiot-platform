FROM fedora:22

MAINTAINER Shilin Ivan <proniment@gmail.com>

ENV SERVICE_NAME data-archiving-service
ENV SERVICE_JAR_NAME data-archiving-service-1.0-SNAPSHOT-jar-with-dependencies

RUN dnf install -y unzip java-1.8.0-openjdk-headless && dnf clean all

WORKDIR /root/semiot-platform/$SERVICE_NAME

# Add inspectIT's agent
ADD https://github.com/inspectIT/inspectIT/releases/download/1.6.7.79/inspectit-agent-sun1.5.zip .
RUN unzip inspectit-agent-sun1.5.zip -d inspectit
ADD inspectit-agent.cfg inspectit/agent/config/inspectit-agent.cfg

ADD target/$SERVICE_JAR_NAME.jar $SERVICE_JAR_NAME.jar

ADD run.sh ./
RUN chmod 777 run.sh

CMD ./run.sh
