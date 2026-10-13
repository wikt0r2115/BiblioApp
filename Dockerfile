# syntax=docker/dockerfile:1.7
FROM eclipse-temurin:25-jdk-alpine AS build

WORKDIR /workspace
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw
COPY src src
RUN --mount=type=cache,target=/root/.m2 ./mvnw -B -Dmaven.test.skip=true package

FROM eclipse-temurin:25-jre-alpine

WORKDIR /app
COPY --from=build /workspace/target/library-1.0.0.jar app.jar
USER 10001:10001
EXPOSE 8080
HEALTHCHECK --interval=10s --timeout=3s --start-period=60s --retries=10 \
  CMD wget -q -O /dev/null http://127.0.0.1:8080/book || exit 1
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
