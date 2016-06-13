#!/bin/sh

java -javaagent:inspectit/agent/inspectit-agent.jar -Dinspectit.config=inspectit/agent/config/ -Xbootclasspath/p:inspectit/agent/inspectit-agent.jar -jar $SERVICE_JAR_NAME.jar
