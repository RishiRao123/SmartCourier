
FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

COPY target/*.jar app.jar

ENTRYPOINT ["java", "-Duser.timezone=UTC", "-jar", "app.jar"]
