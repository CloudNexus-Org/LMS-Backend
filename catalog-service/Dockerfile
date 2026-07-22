FROM eclipse-temurin:21-jre
WORKDIR /app
COPY catalog-service/target/catalog-service-*.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]
