#!/bin/bash -x

echo "server.port=$SERVER_PORT
spring.data.mongodb.host=$MONGODB_HOST
spring.data.mongodb.port=$MONGODB_PORT
spring.data.mongodb.database=$CALLISTO_DATABASE"


cd $CALLISTO_HOME
exec java -cp $CALLISTO_HOME/*.jar \
	-Dloader.path=$CALLISTO_HOME/lib/* \
	-Dserver.port=$SERVER_PORT \
	-Dspring.data.mongodb.host=$MONGODB_HOST \
	-Dspring.data.mongodb.port=$MONGODB_PORT \
	-javaagent:$CALLISTO_HOME/jmx/jmx_prometheus_javaagent-0.7.jar=$JAVA_AGENT_PORT:$CALLISTO_HOME/jmx/jmx_exporter.json \
	-Dspring.data.mongodb.database=$CALLISTO_DATABASE \
	-javaagent:$CALLISTO_HOME/jmx/elastic-apm-agent-0.6.0.jar \
	-Delastic.apm.service_name=$SERVICE_NAME \
    -Delastic.apm.application_packages=com.logistimo.callisto \
    -Delastic.apm.server_url=http://$APM_SERVER_URL \
	-Dloader.main=com.logistimo.callisto.CallistoApplication org.springframework.boot.loader.PropertiesLauncher
