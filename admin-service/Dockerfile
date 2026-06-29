FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY admin-service/pom.xml admin-service/pom.xml
RUN mvn -N install -q
RUN mvn -f admin-service/pom.xml dependency:go-offline -q
COPY admin-service admin-service
RUN mvn -f admin-service/pom.xml package -DskipTests -q

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/admin-service/target/*.jar app.jar
EXPOSE 8094
ENTRYPOINT ["java", "-jar", "app.jar"]
