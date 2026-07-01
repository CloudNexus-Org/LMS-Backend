FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY enrollment-service/pom.xml enrollment-service/pom.xml
RUN mvn -N install -q
RUN mvn -f enrollment-service/pom.xml dependency:go-offline -q
COPY enrollment-service enrollment-service
RUN mvn -f enrollment-service/pom.xml package -DskipTests -q

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/enrollment-service/target/*.jar app.jar
EXPOSE 8086
ENTRYPOINT ["java", "-jar", "app.jar"]
