FROM jboss/wildfly:9.0.2.Final
USER root

ENV WILDFLY_HOME=/opt/jboss/wildfly
RUN $WILDFLY_HOME/bin/add-user.sh admin admin --silent

#Install MySQL Connector
ENV MYSQL_CONNECTOR http://central.maven.org/maven2/mysql/mysql-connector-java/5.1.35/mysql-connector-java-5.1.35.jar
RUN rm -rf $WILDFLY_HOME/welcome-content && mkdir -p $WILDFLY_HOME/modules/system/layers/base/mysql/mysql-connector-java/main/
ADD $MYSQL_CONNECTOR $WILDFLY_HOME/modules/system/layers/base/mysql/mysql-connector-java/main/
ADD module.xml $WILDFLY_HOME/modules/system/layers/base/mysql/mysql-connector-java/main/

#Install a key store
WORKDIR $WILDFLY_HOME/standalone/configuration
RUN keytool -genkey -noprompt \
    -alias server \
    -dname "CN=localhost, OU=ID, O=Unknown, L=Unknown, S=Unknown, C=NA" \
    -keyalg RSA -keystore server.keystore -validity 365 \
    -storepass password \
    -keypass password

ADD standalone.xml $WILDFLY_HOME/standalone/configuration/
ADD standalone.conf $WILDFLY_HOME/bin/

# Add inspectIT's agent
ADD https://github.com/inspectIT/inspectIT/releases/download/1.6.7.79/inspectit-agent-sun1.5.zip $WILDFLY_HOME/
RUN unzip $WILDFLY_HOME/inspectit-agent-sun1.5.zip -d $WILDFLY_HOME/inspectit
ADD inspectit-agent.cfg $WILDFLY_HOME/inspectit/agent/config/inspectit-agent.cfg

ADD target/api-gateway-1.0-SNAPSHOT/ $WILDFLY_HOME/standalone/deployments/api-gateway-1.0-SNAPSHOT.war/