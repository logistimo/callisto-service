#!/bin/bash -x

echo "server.port=$SERVER_PORT
spring.data.mongodb.host=$MONGODB_HOST
spring.data.mongodb.port=$MONGODB_PORT
spring.data.mongodb.database=$CALLISTO_DATABASE"

exec java -cp $TOMCAT_HOME/webapps/callisto-application-0.1.0-SNAPSHOT.jar \
	-Dloader.path=$TOMCAT_HOME/lib/callisto-plugins-functions-0.1.0-SNAPSHOT.jar \
	-Dloader.main=com.logistimo.callisto.CallistoApplication org.springframework.boot.loader.PropertiesLauncher \
	-Dserver.port=$SERVER_PORT \
	-Dspring.data.mongodb.host=$MONGODB_HOST \
	-Dspring.data.mongodb.port=$MONGODB_PORT \
	-javaagent:$TOMCAT_HOME/jmx_prometheus_javaagent-0.7.jar=$JAVA_AGENT_PORT:$TOMCAT_HOME/jmx_exporter.json \
	-Dspring.data.mongodb.database=$CALLISTO_DATABASE