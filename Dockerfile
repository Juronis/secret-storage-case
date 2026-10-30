FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN apt-get update && apt-get install -y maven && mvn package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
# Credentials are runtime-only: docker run -e ADMIN_USERNAME=... -e ADMIN_PASSWORD=...
ENTRYPOINT ["java", "-jar", "app.jar"]
