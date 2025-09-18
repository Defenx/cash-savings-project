# syntax=docker/dockerfile:1.7

ARG JDK_IMAGE=eclipse-temurin:21-jdk
ARG RUNTIME_IMAGE=eclipse-temurin:21-jre-alpine

FROM ${JDK_IMAGE} AS build
WORKDIR /workspace

ENV GRADLE_USER_HOME=/workspace/.gradle

COPY gradle/wrapper gradle/wrapper
COPY gradlew gradlew
COPY gradle/libs.versions.toml gradle/libs.versions.toml
COPY settings.gradle.kts settings.gradle.kts
COPY build.gradle.kts build.gradle.kts

COPY app/build.gradle.kts app/build.gradle.kts

RUN chmod +x ./gradlew

COPY . .
RUN --mount=type=cache,target=/workspace/.gradle \
    ./gradlew --no-daemon clean :app:bootJar -x test

FROM ${RUNTIME_IMAGE} AS runtime
WORKDIR /app

RUN addgroup -S app && adduser -S app -G app
USER app

ENV TZ=UTC

COPY --from=build /workspace/app/build/libs/app.jar /app/app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]