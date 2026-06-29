FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY review-service/pom.xml review-service/pom.xml
RUN mvn -N install -q
RUN mvn -f review-service/pom.xml dependency:go-offline -q
COPY review-service review-service
RUN mvn -f review-service/pom.xml package -DskipTests -q

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/review-service/target/*.jar app.jar
EXPOSE 8091
ENTRYPOINT ["java", "-jar", "app.jar"]
