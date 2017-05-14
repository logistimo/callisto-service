#!/bin/bash -x

echo "server.port=$SERVER_PORT
spring.data.mongodb.host=$MONGODB_HOST
spring.data.mongodb.port=$MONGODB_PORT
spring.data.mongodb.database=$CALLISTO_DATABASE"

exec java -Dserver.port=$SERVER_PORT \
	-Dspring.data.mongodb.host=$MONGODB_HOST \
	-Dspring.data.mongodb.port=$MONGODB_PORT \
	-Dspring.data.mongodb.database=$CALLISTO_DATABASE \
	-javaagent:$TOMCAT_HOME/jmx_prometheus_javaagent-0.7.jar=$JAVA_AGENT_PORT:$TOMCAT_HOME/jmx_exporter.json \
	-jar $TOMCAT_HOME/webapps/*
