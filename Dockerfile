FROM docker.logistimo.com:8082/logi-tomcat:8.5
MAINTAINER naren <naren@logistimo.com>

EXPOSE 9080

EXPOSE 9088

ARG APP_NAME
ARG APP_VERSION

ENV TOMCAT_HOME /usr/local/tomcat 

ENV JAVA_AGENT_PORT 9088

ENV SERVER_PORT=9080 \
        MONGODB_HOST=localhost \
        MONGODB_PORT=27017 \
        CALLISTO_DATABASE=callisto	

VOLUME $TOMCAT_HOME/logs

RUN rm -rf $TOMCAT_HOME/webapps/* 

ADD modules/${APP_NAME}/target/${APP_NAME}-${APP_VERSION}.jar $TOMCAT_HOME/webapps/

ADD jmx_prometheus_javaagent-0.7.jar $TOMCAT_HOME/
ADD jmx_exporter.json $TOMCAT_HOME/

COPY docker-entrypoint.sh /docker-entrypoint.sh

RUN chmod +x /docker-entrypoint.sh

CMD ["/docker-entrypoint.sh"]

