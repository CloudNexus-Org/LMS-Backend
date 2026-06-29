FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY catalog-service/pom.xml catalog-service/pom.xml
RUN mvn -N install -q
RUN mvn -f catalog-service/pom.xml dependency:go-offline -q
COPY catalog-service catalog-service
RUN mvn -f catalog-service/pom.xml package -DskipTests -q

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/catalog-service/target/*.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]
