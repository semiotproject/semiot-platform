FROM fedora

ENV SERVICE_NAME device-proxy-service
ENV SERVICE_JAR_NAME device-proxy-service-1.0-SNAPSHOT-jar-with-dependencies

WORKDIR /

RUN yum upgrade -y && yum clean all
RUN yum install -y java-1.8.0-openjdk-devel maven git && yum clean all

RUN git clone https://github.com/semiotproject/semiot-platform.git

WORKDIR /semiot-platform/$SERVICE_NAME

RUN mvn clean install -DskipTests=true
#Clean maven cache
RUN rm -rf /root/.m2/repository

CMD java -jar target/$SERVICE_JAR_NAME.jar
