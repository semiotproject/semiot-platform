FROM ubuntu
MAINTAINER Shilin Ivan <proniment@gmail.com>

ENV SERVICE_NAME data-archiving-service
ENV SERVICE_JAR_NAME data-archiving-service-1.0-SNAPSHOT-jar-with-dependencies

WORKDIR /	

# Java
RUN apt-get update && apt-get install -y wget binutils java-common unzip && echo oracle-java8-installer shared/accepted-oracle-license-v1-1 select true | /usr/bin/debconf-set-selections && wget https://db.tt/dFU3BqFP -O /root/oracle-java8-installer_8u5-1~webupd8~3_all.deb && dpkg -i oracle-java8-installer_8u5-1~webupd8~3_all.deb

# Utils
RUN apt-get update && apt-get install -y maven git

ENV JAVA_HOME /usr/lib/jvm/java-8-oracle/jre

RUN git clone https://github.com/semiotproject/semiot-platform.git

WORKDIR /semiot-platform/$SERVICE_NAME

RUN mvn clean install -DskipTests=true && rm -rf /root/.m2/repository

CMD java -jar target/$SERVICE_JAR_NAME.jar
