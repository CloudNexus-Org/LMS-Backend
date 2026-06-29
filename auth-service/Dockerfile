FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY auth-service/pom.xml auth-service/pom.xml
RUN mvn -N install -q
RUN mvn -f auth-service/pom.xml dependency:go-offline -q
COPY auth-service auth-service
RUN mvn -f auth-service/pom.xml package -DskipTests -q

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/auth-service/target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
