# Stage 1: Build the server fat JAR
FROM gradle:8.5-jdk17 AS build
WORKDIR /app
COPY gradle/ gradle/
COPY gradlew gradlew.bat build.gradle.kts settings.gradle.kts gradle.properties ./
COPY server/build.gradle.kts server/build.gradle.kts
COPY shared/build.gradle.kts shared/build.gradle.kts
COPY composeApp/build.gradle.kts composeApp/build.gradle.kts
COPY shared/ shared/
COPY server/ server/
RUN chmod +x gradlew && ./gradlew :server:buildFatJar --no-daemon -x test

# Stage 2: Lightweight runtime
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/server/build/libs/server-all.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
