FROM openjdk:8-jre
MAINTAINER dockers@logistimo.com

EXPOSE 9080

EXPOSE 9088

ARG APP_NAME
ARG APP_VERSION

RUN mkdir -p /opt/callisto/lib && mkdir -p /opt/callisto/jmx

ENV CALLISTO_HOME /opt/callisto

ENV JAVA_AGENT_PORT 9088

ENV SERVER_PORT=9080 \
        MONGODB_HOST=localhost \
        MONGODB_PORT=27017 \
        CALLISTO_DATABASE=callisto \
        SERVICE_NAME=logi-callisto \
        JAVA_OPTS="" \
        APM_SERVER_URL=localhost:8200

VOLUME $CALLISTO_HOME/logs

ADD modules/${APP_NAME}/target/${APP_NAME}-${APP_VERSION}.jar $CALLISTO_HOME/

ADD jmx_prometheus_javaagent-0.7.jar $CALLISTO_HOME/jmx/
ADD jmx_exporter.json $CALLISTO_HOME/jmx/

ADD custom-functions/* $CALLISTO_HOME/lib/

COPY docker-entrypoint.sh /docker-entrypoint.sh

RUN wget -P $CALLISTO_HOME/jmx/ http://repo1.maven.org/maven2/co/elastic/apm/elastic-apm-agent/0.6.0/elastic-apm-agent-0.6.0.jar \
        && chmod +x /docker-entrypoint.sh

CMD ["/docker-entrypoint.sh"]

