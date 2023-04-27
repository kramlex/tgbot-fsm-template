
FROM openjdk:17-jdk-slim AS build

WORKDIR /app

COPY . ./

ENV GRADLE_OPTS -Dkotlin.compiler.execution.strategy="in-process"

RUN ./gradlew assembleDist --no-daemon

RUN tar -xf build/distributions/tgbot-fsm-1.tar

FROM openjdk:17-jdk-slim

COPY --from=build /app/kotlin-acept-bot-1 /app

WORKDIR /app

RUN mkdir /app/database

ENTRYPOINT ["/bin/sh", "/app/bin/tgbot-fsm"]