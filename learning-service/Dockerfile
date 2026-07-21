FROM eclipse-temurin:21-jre
WORKDIR /app
COPY learning-service/target/learning-service-*.jar app.jar
EXPOSE 8087
ENTRYPOINT ["java", "-jar", "app.jar"]
