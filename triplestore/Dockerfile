FROM fedora:24

RUN dnf install --refresh -y java-1.8.0-openjdk-headless nss && dnf clean all

WORKDIR /blazegraph

ADD upload-on-start.sh config.properties http://tenet.dl.sourceforge.net/project/bigdata/bigdata/2.1.1/blazegraph.jar ./

ENV TRIPLESTORE_PORT=3030 \
  TRIPLESTORE_UPLOAD_DIR=/var/lib/blazegraph/upload

RUN mkdir -p $TRIPLESTORE_UPLOAD_DIR \
  && chmod 755 upload-on-start.sh

ADD https://raw.githubusercontent.com/semiotproject/ontologies/master/semiot/semiot.ttl $TRIPLESTORE_UPLOAD_DIR/semiot.ttl

EXPOSE $TRIPLESTORE_PORT

CMD bash upload-on-start.sh && \
  java -server \
    -Djetty.port=$TRIPLESTORE_PORT \
    -Dcom.bigdata.rdf.sail.webapp.ConfigParams.propertyFile=/blazegraph/config.properties \
    -jar blazegraph.jar

