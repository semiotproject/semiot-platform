FROM fedora:23

MAINTAINER Shilin Ivan <proniment@gmail.com>

ENV FUSEKI_ARCHIVE_NAME apache-jena-fuseki-2.3.1
ENV FUSEKI_HOME /fuseki
ENV FUSEKI_BASE /fuseki/base

WORKDIR /
RUN echo "fastestmirror=true" >> /etc/dnf/dnf.conf
RUN dnf install -y java-1.8.0-openjdk-headless nss-3.23.0 wget unzip which && dnf clean all

RUN mkdir -p $FUSEKI_BASE
WORKDIR $FUSEKI_HOME

RUN wget http://archive.apache.org/dist/jena/binaries/apache-jena-fuseki-2.3.1.zip
RUN unzip $FUSEKI_ARCHIVE_NAME.zip && rm $FUSEKI_ARCHIVE_NAME.zip
RUN mv $FUSEKI_ARCHIVE_NAME/* . && rm -rf $FUSEKI_ARCHIVE_NAME
ADD shiro.ini $FUSEKI_BASE/
ADD config.ttl $FUSEKI_BASE/
ADD log4j.properties $FUSEKI_BASE/
ADD web.xml $FUSEKI_HOME/webapp/WEB-INF/web.xml
ADD content $FUSEKI_HOME/content

# Add inspectIT's agent
ADD https://github.com/inspectIT/inspectIT/releases/download/1.6.8.82/inspectit-agent-sun1.5-1.6.8.82.zip ./inspectit-agent.zip
RUN unzip inspectit-agent.zip -d inspectit

ENV JVM_ARGS "-javaagent:inspectit/agent/inspectit-agent.jar -Dinspectit.repository=winghouse.semiot.ru:9070 -Dinspectit.agent.name=triplestore -Xbootclasspath/p:inspectit/agent/inspectit-agent.jar"

CMD rm -f $FUSEKI_HOME/fuseki-db/tdb.lock
CMD if [ -e $FUSEKI_HOME/app/shiro.ini ] then rm $FUSEKI_BASE/shiro.ini cp $FUSEKI_HOME/app/shiro.ini $FUSEKI_BASE/shiro.ini fi
CMD ./fuseki-server --config=base/config.ttl
