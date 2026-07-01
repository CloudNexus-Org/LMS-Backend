FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY analytics-service/pom.xml analytics-service/pom.xml
RUN mvn -N install -q
RUN mvn -f analytics-service/pom.xml dependency:go-offline -q
COPY analytics-service analytics-service
RUN mvn -f analytics-service/pom.xml package -DskipTests -q

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/analytics-service/target/*.jar app.jar
EXPOSE 8093
ENTRYPOINT ["java", "-jar", "app.jar"]
