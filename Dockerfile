FROM amazoncorretto:25.0.3-alpine3.23

ENV LANGUAGE='en_US:en'

# CVE-2026-45447: ensure patched libcrypto3 and libssl3 version (>=3.5.7-r0).
# amazoncorretto:25.0.3-alpine3.23 ships vulnerable versions of these packages.
# TODO This line can be removed once a newer amazoncorretto base image includes patched versions.
RUN apk add --no-cache "libcrypto3>=3.5.7-r0" "libssl3>=3.5.7-r0"

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
