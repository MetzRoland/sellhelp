FROM eclipse-temurin:25

WORKDIR /backend

COPY backend/target/sellHelpBackend-0.0.2-SNAPSHOT.jar sellHelpBackend.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "sellHelpBackend.jar"]
