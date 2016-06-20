FROM fedora:23

RUN dnf install -y java-1.8.0-openjdk-headless nss && dnf clean all

WORKDIR /blazegraph

ADD http://tenet.dl.sourceforge.net/project/bigdata/bigdata/2.1.1/blazegraph.jar ./

ADD config.properties ./

EXPOSE 3030

CMD java -server \
  -Djetty.port=3030 \
  -Dcom.bigdata.rdf.sail.webapp.ConfigParams.propertyFile=/blazegraph/config.properties \
  -jar blazegraph.jar
