FROM amazoncorretto:25.0.2-alpine3.23

ENV LANGUAGE='en_US:en'

# CVE-2026-22184: ensure patched zlib version (>=1.3.2).
# CVE-2026-28390: ensure patched openssl version (>=3.5.6-r0).
# CVE-2026-40200: ensure patched musl version (>=1.2.5-r23).
# amazoncorretto:25.0.2-alpine3.23 ships vulnerable versions of these packages.
# TODO This line can be removed once a newer amazoncorretto base image includes patched versions.
RUN apk add --no-cache "zlib>=1.3.2" "libcrypto3>=3.5.6-r0" "libssl3>=3.5.6-r0" "musl>=1.2.5-r23" "musl-utils>=1.2.5-r23"

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
