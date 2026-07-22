FROM eclipse-temurin:21-jre
WORKDIR /app
COPY admin-service/target/admin-service-*.jar app.jar
EXPOSE 8094
ENTRYPOINT ["java", "-jar", "app.jar"]
