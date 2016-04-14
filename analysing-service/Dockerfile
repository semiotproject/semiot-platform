FROM ubuntu

WORKDIR /root

ENV MYSQL_CONNECTOR http://central.maven.org/maven2/mysql/mysql-connector-java/5.1.22/mysql-connector-java-5.1.22.jar

ENV WLDFL http://download.jboss.org/wildfly/8.2.0.Final/wildfly-8.2.0.Final.tar.gz

# Java
RUN apt-get update && apt-get install -y wget binutils java-common unzip && echo oracle-java8-installer shared/accepted-oracle-license-v1-1 select true | /usr/bin/debconf-set-selections && wget https://db.tt/dFU3BqFP -O /root/oracle-java8-installer_8u5-1~webupd8~3_all.deb && dpkg -i oracle-java8-installer_8u5-1~webupd8~3_all.deb && rm oracle-java8-installer_8u5-1~webupd8~3_all.deb

ENV JAVA_HOME /usr/lib/jvm/java-8-oracle/jre

#Setup Wildfly
RUN wget $WLDFL && tar xvzf wildfly-8.2.0.Final.tar.gz && rm wildfly-8.2.0.Final.tar.gz && mkdir -p wildfly-8.2.0.Final/modules/system/layers/base/mysql/mysql-connector-java/main

#Config Wildfly
RUN wget -P wildfly-8.2.0.Final/modules/system/layers/base/mysql/mysql-connector-java/main $MYSQL_CONNECTOR
ADD https://github.com/semiotproject/semiot-platform/raw/master/analysing-service/wildfly_conf/standalone.xml wildfly-8.2.0.Final/standalone/configuration/ 
ADD https://github.com/semiotproject/semiot-platform/raw/master/analysing-service/wildfly_conf/module.xml wildfly-8.2.0.Final/modules/system/layers/base/mysql/mysql-connector-java/main/
#Setup MySQL
RUN apt-get update && export DEBIAN_FRONTEND=noninteractive && apt-get -q -y install mysql-server

#Config MySQL
RUN service mysql start && mysql --execute="create database analyzis; connect analyzis; \
	create table queries(id int(3), name text, query text, sparql text, time timestamp, primary key(id)); \
	create table events(id int(3), query_id int(3), events longtext, time timestamp, primary key(id), foreign key(query_id) references queries(id) on delete cascade);"

WORKDIR /root/wildfly-8.2.0.Final/
#Deploy to Wildfly
ADD target/ROOT.war ./standalone/deployments/

EXPOSE 8085

CMD service mysql start && bin/standalone.sh -b 0.0.0.0
