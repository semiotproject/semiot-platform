FROM fedora:22

MAINTAINER Shilin Ivan <proniment@gmail.com>

ENV SERVICE_NAME device-proxy-service
ENV SERVICE_JAR_NAME device-proxy-service-launcher-1.0-SNAPSHOT-jar-with-dependencies
ENV FUSEKI_ARCHIVE_NAME apache-jena-fuseki-2.3.0
ENV FUSEKI_HOME /fuseki
ENV FUSEKI_BASE /fuseki/base

WORKDIR /
RUN dnf install -y java-1.8.0-openjdk-headless wget unzip supervisor && dnf clean all

#Install fuseki
RUN mkdir -p $FUSEKI_BASE

WORKDIR $FUSEKI_HOME

RUN wget http://archive.apache.org/dist/jena/binaries/apache-jena-fuseki-2.0.0.zip
RUN unzip $FUSEKI_ARCHIVE_NAME.zip && rm $FUSEKI_ARCHIVE_NAME.zip
RUN mv $FUSEKI_ARCHIVE_NAME/* . && rm -rf $FUSEKI_ARCHIVE_NAME
ADD shiro.ini $FUSEKI_BASE/
ADD config.ttl $FUSEKI_BASE/
ADD web.xml $FUSEKI_HOME/webapp/WEB-INF/web.xml

WORKDIR /root/semiot-platform/$SERVICE_NAME

ADD device-proxy-service-launcher/target/$SERVICE_JAR_NAME.jar $SERVICE_JAR_NAME.jar

ADD run.sh ./
RUN chmod 777 run.sh

#Install Supervisord
RUN mkdir -p /var/log/supervisor
ADD supervisor-proxyservice.conf /etc/supervisor/conf.d/proxyservice.conf

CMD ["/usr/bin/supervisord", "-c", "/etc/supervisor/conf.d/proxyservice.conf"]
