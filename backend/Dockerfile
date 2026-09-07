# syntax=docker/dockerfile:1
#
# Multi-stage build for the MentR Spring Boot backend, for deployment on
# Render (Root Directory = /backend, so all paths below are relative to
# the backend/ folder).
#
# No secrets are baked into this image. DB_URL, DB_USERNAME, DB_PASSWORD
# and JWT_SECRET must be set as environment variables on Render — they
# are already read that way by application.properties.

# ---------- Build stage ----------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copy only the POM first so the dependency layer is cached and reused
# on rebuilds where just the source changed.
COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src ./src
RUN mvn -B clean package -DskipTests

# ---------- Runtime stage ----------
FROM eclipse-temurin:17-jre
WORKDIR /app

# Copy the Spring Boot fat jar produced by the build stage.
COPY --from=build /app/target/*.jar app.jar

# Render injects PORT at container start; default to 10000 (Render's
# conventional default) for a plain `docker run` with no PORT set.
ENV PORT=10000
EXPOSE 10000

# Bind to all interfaces and listen on Render's PORT — done via JVM
# system properties at startup, so no application.properties change
# was needed for this.
CMD ["sh", "-c", "java -Dserver.address=0.0.0.0 -Dserver.port=${PORT:-10000} -jar app.jar"]
