ARG MVN_VERSION=3.8.6
ARG JAVA_IMAGE_VERSION=17.0.1
FROM maven:${MVN_VERSION} AS dependencies

# go-offline using the pom.xml
COPY ./common /common
COPY ./agent/pom.xml /agent/pom.xml

WORKDIR /common
RUN --mount=type=cache,target=/root/.m2 mvn dependency:go-offline install

WORKDIR /agent
RUN --mount=type=cache,target=/root/.m2 mvn dependency:go-offline

ARG MVN_VERSION
ARG JAVA_IMAGE_VERSION
FROM dependencies AS builder

COPY ./agent /agent

WORKDIR /agent
RUN --mount=type=cache,target=/root/.m2 mvn clean package -DskipTests


ARG JAVA_IMAGE_VERSION
FROM openjdk:${JAVA_IMAGE_VERSION}-jdk-slim
RUN apt-get update && apt-get install -y curl
COPY --from=builder /agent/target/agent-0.0.1-SNAPSHOT-jar-with-dependencies.jar /
ENTRYPOINT ["java"]
CMD ["-jar","./agent-0.0.1-SNAPSHOT-jar-with-dependencies.jar"]