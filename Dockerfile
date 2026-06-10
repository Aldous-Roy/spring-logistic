FROM eclipse-temurin:21-jdk-jammy AS build

WORKDIR /workspace

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
COPY src/ src/

RUN chmod +x mvnw && ./mvnw -q -DskipTests package && \
    mkdir -p /app && \
    jar="$(find target -maxdepth 1 -name '*.jar' ! -name '*original*' | head -n 1)" && \
    cp "$jar" /app/app.jar

FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

RUN useradd --create-home --uid 10001 appuser && \
    chown -R appuser:appuser /app

COPY --from=build /app/app.jar /app/app.jar

USER appuser

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
