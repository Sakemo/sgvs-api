FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src ./src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /workspace/target/sgvs-api-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8081

CMD ["java", "-jar", "app.jar"]
