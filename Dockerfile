# =============================================================
# NeuroForge Enterprise SDLC Platform — Dockerfile
# Multi-stage build: Maven build → minimal JRE runtime
# =============================================================

# ── Stage 1: Build ────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /workspace

# Copy dependency manifests first for better layer caching
COPY pom.xml .
COPY .mvn/ .mvn/
COPY mvnw .

RUN chmod +x mvnw

# Download dependencies (cached unless pom.xml changes)
RUN ./mvnw dependency:go-offline -B

# Copy source and build the fat JAR (skip tests — run them in CI)
COPY src/ src/
RUN ./mvnw package -B -DskipTests

# ── Stage 2: Runtime ──────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine AS runtime

# Security: run as non-root user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# Copy only the fat JAR from the build stage
COPY --from=build /workspace/target/*.jar app.jar

# Give ownership to the non-root user
RUN chown appuser:appgroup app.jar

USER appuser

# Expose the application port
EXPOSE 8080

# Health check (uses Spring Actuator)
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
    CMD wget -qO- http://localhost:8080/actuator/health || exit 1

# All configuration is supplied via environment variables at runtime.
# Example: docker run -e DB_URL=... -e DB_USERNAME=... -e DB_PASSWORD=... \
#                     -e JWT_SECRET=... -e JWT_ACCESS_TOKEN_EXPIRATION_SECONDS=900 \
#                     -e JWT_REFRESH_TOKEN_EXPIRATION_SECONDS=604800 \
#                     -e WEBAPP_ORIGIN=https://yourdomain.com \
#                     neuroforge:latest
ENTRYPOINT ["java", "-jar", "app.jar"]
