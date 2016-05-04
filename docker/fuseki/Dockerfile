FROM fedora:23

MAINTAINER Shilin Ivan <proniment@gmail.com>

ENV FUSEKI_ARCHIVE_NAME apache-jena-fuseki-2.3.1
ENV FUSEKI_HOME /fuseki
ENV FUSEKI_BASE /fuseki/base

WORKDIR /
RUN dnf install -y java-1.8.0-openjdk-headless wget unzip which && dnf clean all

RUN mkdir -p $FUSEKI_BASE
WORKDIR $FUSEKI_HOME

RUN wget http://archive.apache.org/dist/jena/binaries/apache-jena-fuseki-2.3.1.zip
RUN unzip $FUSEKI_ARCHIVE_NAME.zip && rm $FUSEKI_ARCHIVE_NAME.zip
RUN mv $FUSEKI_ARCHIVE_NAME/* . && rm -rf $FUSEKI_ARCHIVE_NAME
ADD shiro.ini $FUSEKI_BASE/
ADD config.ttl $FUSEKI_BASE/
ADD web.xml $FUSEKI_HOME/webapp/WEB-INF/web.xml

CMD rm -f /fuseki/fuseki-db/tdb.lock
CMD if [ -e $FUSEKI_HOME/app/shiro.ini ] then rm $FUSEKI_BASE/shiro.ini cp /fuseki/app/shiro.ini $FUSEKI_BASE/shiro.ini fi
CMD ./fuseki-server --config=base/config.ttl
