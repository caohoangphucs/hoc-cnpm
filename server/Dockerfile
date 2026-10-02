# syntax=docker/dockerfile:1
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn --batch-mode clean verify && mvn --batch-mode dependency:copy-dependencies -DincludeScope=test -DoutputDirectory=target/test-libs

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/cnpm.jar /app/cnpm.jar
USER 10001:10001
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/cnpm.jar"]
