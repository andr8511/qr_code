#Maven
FROM maven:3.9.9-amazoncorretto-17-al2023 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTest

#JDK
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /app/target/QR_CODE_Generate-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]