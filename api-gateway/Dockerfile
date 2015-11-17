FROM jboss/wildfly

ADD target/api-gateway-1.0-SNAPSHOT.war /opt/jboss/wildfly/standalone/deployments/api-gateway.war

RUN rm -rf /opt/jboss/wildfly/welcome-content
