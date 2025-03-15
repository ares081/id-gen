FROM eclipse-temurin:21-jdk-jammy
LABEL authors="ares"

WORKDIR /app

COPY target/id-gen-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]