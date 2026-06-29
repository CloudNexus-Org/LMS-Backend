FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY mentor-service/pom.xml mentor-service/pom.xml
RUN mvn -N install -q
RUN mvn -f mentor-service/pom.xml dependency:go-offline -q
COPY mentor-service mentor-service
RUN mvn -f mentor-service/pom.xml package -DskipTests -q

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/mentor-service/target/*.jar app.jar
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "app.jar"]
