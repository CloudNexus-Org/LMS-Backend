FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY certificate-service/pom.xml certificate-service/pom.xml
RUN mvn -N install -q
RUN mvn -f certificate-service/pom.xml dependency:go-offline -q
COPY certificate-service certificate-service
RUN mvn -f certificate-service/pom.xml package -DskipTests -q

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/certificate-service/target/*.jar app.jar
EXPOSE 8090
ENTRYPOINT ["java", "-jar", "app.jar"]
