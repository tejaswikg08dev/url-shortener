# Multi-stage build for Analytics Service
# Same pattern as API Service but with smaller memory allocation

# --- Stage 1: Build ---
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

COPY pom.xml .
COPY url-shortener-common/pom.xml url-shortener-common/
COPY url-shortener-api/pom.xml url-shortener-api/
COPY url-shortener-analytics/pom.xml url-shortener-analytics/

RUN mvn dependency:go-offline -pl url-shortener-analytics -am -B

COPY url-shortener-common/src url-shortener-common/src
COPY url-shortener-analytics/src url-shortener-analytics/src

RUN mvn package -pl url-shortener-analytics -am -DskipTests -B

# --- Stage 2: Run ---
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

RUN addgroup -S appgroup && adduser -S appuser -G appgroup
COPY --from=build /app/url-shortener-analytics/target/*.jar app.jar
USER appuser

EXPOSE 8081

HEALTHCHECK --interval=30s --timeout=5s --retries=3 \
  CMD wget -qO- http://localhost:8081/actuator/health || exit 1

# Smaller heap: analytics service is lighter than API service
ENTRYPOINT ["java", "-Xmx256m", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]