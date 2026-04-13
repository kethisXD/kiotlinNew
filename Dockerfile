FROM gradle:8.7.0-jdk17 AS cache
RUN mkdir -p /home/gradle/cache_home
ENV GRADLE_USER_HOME /home/gradle/cache_home
COPY build.gradle.kts settings.gradle.kts gradle.properties /home/gradle/app/
COPY domain/build.gradle.kts /home/gradle/app/domain/
COPY data/build.gradle.kts /home/gradle/app/data/
COPY service/build.gradle.kts /home/gradle/app/service/
COPY api/build.gradle.kts /home/gradle/app/api/
WORKDIR /home/gradle/app
RUN gradle clean build -i --stacktrace --no-daemon -x test

FROM gradle:8.7.0-jdk17 AS build
COPY --from=cache /home/gradle/cache_home /home/gradle/.gradle
COPY . /usr/src/app
WORKDIR /usr/src/app
RUN gradle installDist --no-daemon -x test

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /usr/src/app/api/build/install/api /app/
EXPOSE 8080
CMD ["./bin/api"]
