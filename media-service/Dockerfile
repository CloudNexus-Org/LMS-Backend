FROM eclipse-temurin:21-jre
WORKDIR /app
COPY media-service/target/media-service-*.jar app.jar
EXPOSE 8095
ENTRYPOINT ["java", "-jar", "app.jar"]
