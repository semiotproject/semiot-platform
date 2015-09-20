FROM fedora

MAINTAINER Shilin Ivan <proniment@gmail.com>

ENV SERVICE_NAME device-proxy-service
ENV SERVICE_JAR_NAME device-proxy-service-launcher-1.0-SNAPSHOT-jar-with-dependencies

WORKDIR /root

RUN yum upgrade -y && yum install -y java-1.8.0-openjdk-devel maven git wget unzip which && yum clean all

RUN git clone -b device-proxy-on-osgi https://github.com/semiotproject/semiot-platform.git

WORKDIR /root/semiot-platform/$SERVICE_NAME

RUN mvn clean install -DskipTests=true && rm -rf /root/.m2/repository

CMD java -jar device-proxy-service-launcher/target/$SERVICE_JAR_NAME.jar