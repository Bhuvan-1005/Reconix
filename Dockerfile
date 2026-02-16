# Stage 1: Build the server fat JAR
FROM gradle:8.5-jdk17 AS build

WORKDIR /app

# Copy Gradle config files first for better layer caching
COPY gradle/ gradle/
COPY gradlew gradlew.bat build.gradle.kts settings.gradle.kts gradle.properties ./

# Copy module build files
COPY server/build.gradle.kts server/build.gradle.kts
COPY shared/build.gradle.kts shared/build.gradle.kts
COPY composeApp/build.gradle.kts composeApp/build.gradle.kts

# Copy source code (only server + shared needed for backend)
COPY shared/ shared/
COPY server/ server/

# Build the fat JAR (skip tests for faster builds)
RUN ./gradlew :server:buildFatJar --no-daemon -x test

# Stage 2: Lightweight runtime
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copy the built JAR from build stage
COPY --from=build /app/server/build/libs/server-all.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
