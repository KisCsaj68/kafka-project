ARG MVN_VERSION=3.8.6
ARG JAVA_IMAGE_VERSION=17.0.1
FROM maven:${MVN_VERSION} AS dependencies

# go-offline using the pom.xml
COPY ./common /common
COPY ./api/pom.xml /api/pom.xml

WORKDIR /common
RUN --mount=type=cache,target=/root/.m2 mvn dependency:go-offline install

WORKDIR /api
RUN --mount=type=cache,target=/root/.m2 mvn dependency:go-offline

ARG MVN_VERSION
ARG JAVA_IMAGE_VERSION
FROM dependencies AS builder

COPY ./api /api

WORKDIR /api
RUN --mount=type=cache,target=/root/.m2 mvn clean package -DskipTests


ARG JAVA_IMAGE_VERSION
FROM openjdk:${JAVA_IMAGE_VERSION}-jdk-slim
RUN apt-get update && apt-get install -y curl
COPY --from=builder /api/target/api-0.0.1-SNAPSHOT-jar-with-dependencies.jar /
ENTRYPOINT ["java"]
CMD ["-jar","./api-0.0.1-SNAPSHOT-jar-with-dependencies.jar"]