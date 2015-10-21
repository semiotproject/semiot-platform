FROM fedora

MAINTAINER Shilin Ivan <proniment@gmail.com>

ENV SERVICE_NAME device-proxy-service
ENV SERVICE_JAR_NAME device-proxy-service-launcher-1.0-SNAPSHOT-jar-with-dependencies

WORKDIR /root

RUN yum upgrade -y && yum install -y java-1.8.0-openjdk-devel maven git wget unzip which && yum clean all

#
# No cache workaround. Once the content of build.number file changed, the cache will be invalidated.
#

ADD build.number /root/build.number

RUN git clone https://github.com/semiotproject/semiot-platform.git

WORKDIR /root/semiot-platform/$SERVICE_NAME

RUN mvn clean install -DskipTests=true && rm -rf /root/.m2/repository

ADD run.sh ./
RUN chmod 777 run.sh

CMD ./run.sh
