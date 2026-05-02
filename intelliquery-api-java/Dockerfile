# ==========================================
# Stage 1: Build
# ==========================================
FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /build

# Copy pom first for better layer caching
COPY pom.xml .

# Download dependencies first
RUN mvn dependency:go-offline -DskipTests

# Copy source code
COPY src ./src

# Build application
RUN ls -la
RUN ls -la src
RUN mvn clean package -DskipTests -Djacoco.skip=true

# Extract Spring Boot layered jar
RUN mkdir -p extracted && \
    java -Djarmode=layertools -jar target/*.jar extract --destination extracted

# ==========================================
# Stage 2: Runtime
# ==========================================
FROM eclipse-temurin:21-jre-alpine AS runtime

# Non-root user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

USER appuser
WORKDIR /app

ENV JAVA_OPTS="-XX:+UseContainerSupport \
-XX:MaxRAMPercentage=75.0 \
-Djava.security.egd=file:/dev/./urandom \
-Dspring.profiles.active=prod"

COPY --from=builder /build/extracted/dependencies ./
COPY --from=builder /build/extracted/spring-boot-loader ./
COPY --from=builder /build/extracted/snapshot-dependencies ./
COPY --from=builder /build/extracted/application ./

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
CMD wget -q --spider http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS org.springframework.boot.loader.launch.JarLauncher"]