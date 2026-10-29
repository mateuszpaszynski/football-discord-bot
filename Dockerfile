FROM eclipse-temurin:21-jre
WORKDIR /app
COPY target/football-discord-bot-1.1.0.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]