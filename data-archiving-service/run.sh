#!/bin/sh

java -javaagent:inspectit/agent/inspectit-agent.jar -Dinspectit.repository=winghouse.semiot.ru:9070 -Dinspectit.agent.name=tsdbservice -Xbootclasspath/p:inspectit/agent/inspectit-agent.jar -jar $SERVICE_JAR_NAME.jar
