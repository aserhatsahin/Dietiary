# syntax=docker/dockerfile:1

# ---------- Build stage: compile and split the Spring Boot jar into layers ----------
FROM eclipse-temurin:17-jdk-noble AS build
WORKDIR /workspace

# Dependencies change less often than code; resolving them first keeps this layer cached.
COPY gradlew settings.gradle build.gradle ./
COPY gradle gradle
RUN ./gradlew --no-daemon --quiet dependencies > /dev/null

COPY src src
# Tests run in CI before this image is built, so they are skipped here.
RUN ./gradlew --no-daemon --quiet bootJar -x test \
    && java -Djarmode=tools -jar build/libs/*.jar extract --layers --launcher --destination extracted

# ---------- Runtime stage: JRE only, non-root ----------
FROM eclipse-temurin:17-jre-noble
RUN groupadd --system app && useradd --system --gid app --no-create-home app
WORKDIR /app

# One layer per Spring Boot layer, least to most frequently changed
COPY --from=build /workspace/extracted/dependencies/ ./
COPY --from=build /workspace/extracted/spring-boot-loader/ ./
COPY --from=build /workspace/extracted/snapshot-dependencies/ ./
COPY --from=build /workspace/extracted/application/ ./

USER app
EXPOSE 8080
# Size the heap from the container's memory limit instead of the host's
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "org.springframework.boot.loader.launch.JarLauncher"]
