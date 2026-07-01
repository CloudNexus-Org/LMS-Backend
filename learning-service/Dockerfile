FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY learning-service/pom.xml learning-service/pom.xml
RUN mvn -N install -q
RUN mvn -f learning-service/pom.xml dependency:go-offline -q
COPY learning-service learning-service
RUN mvn -f learning-service/pom.xml package -DskipTests -q

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/learning-service/target/*.jar app.jar
EXPOSE 8087
ENTRYPOINT ["java", "-jar", "app.jar"]
