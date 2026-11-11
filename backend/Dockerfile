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
    && cp build/libs/*.jar application.jar \
    && java -Djarmode=tools -jar application.jar extract --layers --destination extracted

# ---------- Runtime stage: JRE only, non-root ----------
FROM eclipse-temurin:17-jre-noble
RUN groupadd --system app && useradd --system --gid app --no-create-home app
WORKDIR /app

# One layer per Spring Boot layer, least to most frequently changed
COPY --from=build /workspace/extracted/dependencies/ ./
COPY --from=build /workspace/extracted/spring-boot-loader/ ./
COPY --from=build /workspace/extracted/snapshot-dependencies/ ./
COPY --from=build /workspace/extracted/application/ ./

# Class Data Sharing: start the app once at build time and archive the classes it loads,
# so real startups skip that work (it matters on a 0.1 CPU free instance).
# The training run stops right after the context is built and needs no database:
# Flyway is off and Hibernate is told the dialect instead of asking a connection.
RUN java -XX:ArchiveClassesAtExit=application.jsa \
        -Dspring.context.exit=onRefresh \
        -Dspring.datasource.url=jdbc:postgresql://training-run.invalid/none \
        -Dspring.flyway.enabled=false \
        -Dspring.jpa.hibernate.ddl-auto=none \
        -Dspring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect \
        -Dspring.jpa.properties.hibernate.boot.allow_jdbc_metadata_access=false \
        -Djwt.secret=cds-training-run-only-not-a-real-secret-0123 \
        -jar application.jar

USER app
EXPOSE 8080
# JVM flags live in JAVA_TOOL_OPTIONS so a platform can replace them (render.yaml does, for 512 MB).
# Default: size the heap from the container's memory limit instead of the host's.
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75"
ENTRYPOINT ["java", "-XX:SharedArchiveFile=application.jsa", "-jar", "application.jar"]
