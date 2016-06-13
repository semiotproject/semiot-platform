FROM fedora:23

MAINTAINER Shilin Ivan <proniment@gmail.com>

ENV SERVICE_NAME device-proxy-service
ENV SERVICE_JAR_NAME device-proxy-service-launcher-1.0-SNAPSHOT-jar-with-dependencies

WORKDIR /
RUN dnf makecache && dnf install -y unzip java-1.8.0-openjdk-headless nss && dnf clean all

WORKDIR /root/semiot-platform/$SERVICE_NAME

# Add inspectIT's agent
ADD https://github.com/inspectIT/inspectIT/releases/download/1.6.7.79/inspectit-agent-sun1.5.zip .
RUN unzip inspectit-agent-sun1.5.zip -d inspectit
ADD inspectit-agent.cfg inspectit/agent/config/inspectit-agent.cfg

ADD device-proxy-service-launcher/target/$SERVICE_JAR_NAME.jar $SERVICE_JAR_NAME.jar
ADD logback.xml ./

CMD java \
    -javaagent:inspectit/agent/inspectit-agent.jar \
     -Dinspectit.config=inspectit/agent/config/ \
     -Xbootclasspath/p:inspectit/agent/inspectit-agent.jar \
     -Dlogback.configurationFile=/root/semiot-platform/$SERVICE_NAME/logback.xml \
     -jar $SERVICE_JAR_NAME.jar
