# ============================================
# Multi-stage build for API Service
#
# Stage 1 (build): Maven compiles Java code into a JAR file
# Stage 2 (run): Slim JRE runs the JAR file
#
# Why two stages?
# Build stage has Maven plus JDK (about 800MB)
# Run stage has only JRE (about 200MB)
# Final image is small and has no build tools (more secure)
# ============================================

# --- Stage 1: Build ---
FROM maven:3.9-eclipse-temurin-17 AS build
# Base image: Maven 3.9 plus Java 17 JDK
# AS build: names this stage so we can copy files from it later

WORKDIR /app
# All commands below run inside /app directory

# Copy POM files first (dependency caching trick)
# Docker caches each instruction as a layer
# If POMs have not changed, this layer is reused from cache
# Dependencies are NOT re-downloaded on every code change
COPY pom.xml .
COPY url-shortener-common/pom.xml url-shortener-common/
COPY url-shortener-api/pom.xml url-shortener-api/
COPY url-shortener-analytics/pom.xml url-shortener-analytics/

# Download all dependencies (cached if POMs unchanged)
RUN mvn dependency:go-offline -pl url-shortener-api -am -B
# -pl url-shortener-api: only this module
# -am: also build modules it depends on (common)
# -B: batch mode (no interactive prompts)

# Now copy source code (changes frequently so this layer rebuilds often)
COPY url-shortener-common/src url-shortener-common/src
COPY url-shortener-api/src url-shortener-api/src

# Build the JAR (skip tests because CI already ran them)
RUN mvn package -pl url-shortener-api -am -DskipTests -B
# Output: url-shortener-api/target/url-shortener-api-1.0.0-SNAPSHOT.jar

# --- Stage 2: Run ---
FROM eclipse-temurin:17-jre-alpine
# Base image: Java 17 JRE only (no JDK, no Maven)
# alpine = tiny Linux distribution (about 5MB base)
# Final image is roughly 200MB instead of 800MB

WORKDIR /app

# Create a non-root user (security best practice)
# Never run containers as root because if container is compromised
# the attacker has root access to the container filesystem
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy ONLY the JAR from the build stage
# Everything else (Maven, source code, dependencies) is discarded
COPY --from=build /app/url-shortener-api/target/*.jar app.jar

# Switch to non-root user
USER appuser

# Document which port the app uses
EXPOSE 8080

# Health check: Docker checks this every 30 seconds
# If it fails 3 times in a row the container is marked unhealthy
# ECS or Docker Compose can restart unhealthy containers
HEALTHCHECK --interval=30s --timeout=5s --retries=3 \
  CMD wget -qO- http://localhost:8080/actuator/health || exit 1

# Start the application
# -Xmx512m: limit Java heap to 512MB (EC2 t2.micro has 1GB total)
# -Djava.security.egd: faster random number generation in containers
ENTRYPOINT ["java", "-Xmx512m", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]