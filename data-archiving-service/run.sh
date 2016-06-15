#!/bin/sh

java -javaagent:inspectit/agent/inspectit-agent.jar -Dinspectit.repository=$INSPECTIT_REPOSITORY -Dinspectit.agent.name=tsdbservice -Xbootclasspath/p:inspectit/agent/inspectit-agent.jar -jar $SERVICE_JAR_NAME.jar
