FROM fedora

ENV FUSEKI_ARCHIVE_NAME apache-jena-fuseki-2.0.0
ENV FUSEKI_HOME /fuseki
ENV FUSEKI_BASE /fuseki/base
ENV SERVICE_NAME device-directory-service
ENV SERVICE_JAR_NAME device-directory-service-1.0-SNAPSHOT-jar-with-dependencies

WORKDIR /

RUN dnf install -y java-1.8.0-openjdk-headless wget unzip && dnf clean all

#Install fuseki
RUN mkdir -p $FUSEKI_BASE

WORKDIR $FUSEKI_HOME

RUN wget http://archive.apache.org/dist/jena/binaries/apache-jena-fuseki-2.0.0.zip
RUN unzip $FUSEKI_ARCHIVE_NAME.zip && rm $FUSEKI_ARCHIVE_NAME.zip
RUN mv $FUSEKI_ARCHIVE_NAME/* . && rm -rf $FUSEKI_ARCHIVE_NAME
ADD shiro.ini $FUSEKI_BASE/
ADD config.ttl $FUSEKI_BASE/
ADD web.xml $FUSEKI_HOME/webapp/WEB-INF/web.xml

WORKDIR /

ADD target/$SERVICE_JAR_NAME.jar $SERVICE_JAR_NAME.jar

ADD run.sh ./
RUN chmod 777 run.sh

CMD ./run.sh
