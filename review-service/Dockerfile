FROM eclipse-temurin:21-jre
WORKDIR /app
COPY review-service/target/review-service-*.jar app.jar
EXPOSE 8091
ENTRYPOINT ["java", "-jar", "app.jar"]
