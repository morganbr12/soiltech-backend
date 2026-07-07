FROM eclipse-temurin:22-jdk AS builder
WORKDIR /app

COPY gradlew .
COPY gradle gradle
RUN chmod +x gradlew

COPY build.gradle.kts settings.gradle.kts ./
RUN ./gradlew dependencies --no-daemon -q

COPY src src
RUN ./gradlew bootJar -x test --no-daemon

FROM eclipse-temurin:22-jre
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
