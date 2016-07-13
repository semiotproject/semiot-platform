#!/bin/sh

java $JMX_OPTS -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/root/semiot-platform/$SERVICE_NAME/dump/dump.hprof -jar $SERVICE_JAR_NAME.jar
