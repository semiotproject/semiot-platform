FROM fedora

MAINTAINER Shilin Ivan <proniment@gmail.com>

ENV SERVICE_NAME data-archiving-service
ENV SERVICE_JAR_NAME data-archiving-service-1.0-SNAPSHOT-jar-with-dependencies

WORKDIR /

RUN yum upgrade -y && yum install -y java-1.8.0-openjdk-devel maven git wget unzip which && yum clean all

RUN git clone https://github.com/semiotproject/semiot-platform.git

WORKDIR /semiot-platform/$SERVICE_NAME

RUN mvn clean install -DskipTests=true && rm -rf /root/.m2/repository

ADD config.properties /semiot-platform/data-archiving-service/config.properties

CMD java -jar target/$SERVICE_JAR_NAME.jar
