FROM jboss/wildfly:9.0.2.Final
USER root

RUN /opt/jboss/wildfly/bin/add-user.sh admin admin --silent

ENV MYSQL_CONNECTOR http://central.maven.org/maven2/mysql/mysql-connector-java/5.1.35/mysql-connector-java-5.1.35.jar
RUN rm -rf /opt/jboss/wildfly/welcome-content && mkdir -p /opt/jboss/wildfly/modules/system/layers/base/mysql/mysql-connector-java/main/

ADD standalone.xml /opt/jboss/wildfly/standalone/configuration/
ADD module.xml /opt/jboss/wildfly/modules/system/layers/base/mysql/mysql-connector-java/main/
ADD $MYSQL_CONNECTOR /opt/jboss/wildfly/modules/system/layers/base/mysql/mysql-connector-java/main/

ADD target/api-gateway-1.0-SNAPSHOT.war /opt/jboss/wildfly/standalone/deployments/api-gateway.war