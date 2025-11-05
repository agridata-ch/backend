FROM amazoncorretto:25.0.1

ENV LANGUAGE='en_US:en'

# We make four distinct layers so if there are application changes the library layers can be re-used
COPY target/quarkus-app/lib/ /deployments/lib/
COPY target/quarkus-app/*.jar /deployments/
COPY target/quarkus-app/app/ /deployments/app/
COPY target/quarkus-app/quarkus/ /deployments/quarkus/

COPY scripts/copy-certificates.sh /deployments/
RUN chmod +x /deployments/copy-certificates.sh \
    && mkdir -p /certs \
    && chown 185:185 /certs

EXPOSE 8060
USER 185
ENV JAVA_OPTS="-Dquarkus.http.host=0.0.0.0 -Djava.util.logging.manager=org.jboss.logmanager.LogManager"
ENV JAVA_APP_JAR="/deployments/quarkus-run.jar"

ENTRYPOINT ["sh", "-c", "/deployments/copy-certificates.sh && java $JAVA_OPTS -jar $JAVA_APP_JAR"]
