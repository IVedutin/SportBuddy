# --- Build stage: compile and package the Spring Boot jar ---
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Cache dependencies first for faster rebuilds.
COPY pom.xml .
COPY .mvn/ .mvn/
RUN mvn -B -q dependency:go-offline

COPY src ./src
# Tests use Testcontainers (a Docker daemon), which is not available inside the
# image build — they run in CI instead. Build the runnable jar here.
RUN mvn -B -q clean package -DskipTests

# --- Runtime stage: slim JRE with just the jar ---
FROM eclipse-temurin:21-jre
WORKDIR /app
RUN groupadd --system spring && useradd --system --gid spring spring
COPY --from=build /app/target/*.jar app.jar
USER spring
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
