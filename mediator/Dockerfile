FROM eclipse-temurin:21 as jre-build

# Create a custom Java runtime
RUN $JAVA_HOME/bin/jlink \
         --add-modules ALL-MODULE-PATH \
         --strip-debug \
         --no-man-pages \
         --no-header-files \
         --compress=2 \
         --output /javaruntime

# cprof
FROM debian:buster-slim as cprof
RUN apt-get update && apt-get install -y wget && rm -rf /var/lib/apt/lists/*

# Runtime
FROM debian:buster-slim

ENV TZ="Europe/Oslo"
ENV JAVA_HOME=/opt/java/openjdk
ENV PATH "${JAVA_HOME}/bin:${PATH}"

COPY --from=jre-build /javaruntime $JAVA_HOME

COPY build/install/* /

USER nobody
CMD ["mediator"]