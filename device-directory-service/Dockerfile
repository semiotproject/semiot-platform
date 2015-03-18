FROM fedora

ENV FUSEKI_ARCHIVE_NAME apache-jena-fuseki-2.0.0
ENV FUSEKI_HOME /fuseki
ENV FUSEKI_BASE /fuseki/base
ENV SERVICE_JAR_NAME device-directory-service-1.0-SNAPSHOT-jar-with-dependencies

WORKDIR /

RUN yum upgrade -y && yum clean all
RUN yum install -y java-1.8.0-openjdk-devel maven git wget unzip which && yum clean all

#Remove Java 7 which is a dependency for maven package
RUN rpm -e --nodeps java-1.7.0-openjdk
RUN rpm -e --nodeps java-1.7.0-openjdk-headless

#Install fuseki
RUN mkdir -p $FUSEKI_BASE

WORKDIR $FUSEKI_HOME

RUN wget http://apache-mirror.rbc.ru/pub/apache/jena/binaries/apache-jena-fuseki-2.0.0.zip
RUN unzip $FUSEKI_ARCHIVE_NAME.zip && rm $FUSEKI_ARCHIVE_NAME.zip
RUN mv $FUSEKI_ARCHIVE_NAME/* . && rm -rf $FUSEKI_ARCHIVE_NAME
RUN cp fuseki /etc/init.d/fuseki
RUN chkconfig --add fuseki
RUN chkconfig --level 2345 fuseki on

WORKDIR /

#Install Device Directory Service
RUN git clone https://github.com/semiotproject/semiot-platform.git

WORKDIR /semiot-platform/device-directory-service

RUN mvn clean install -DskipTests=true
#Clean maven cache
RUN rm -rf /root/.m2/repository

CMD /etc/init.d/fuseki start && java -jar target/$SERVICE_JAR_NAME.jar
