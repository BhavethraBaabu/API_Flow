# ---- Build stage ----
    FROM maven:3.9-eclipse-temurin-21 AS build
    WORKDIR /app
    COPY pom.xml .
    RUN mvn -B dependency:go-offline
    COPY src ./src
    RUN mvn -B clean package -DskipTests
    
    # ---- Runtime stage ----
    FROM eclipse-temurin:21-jre-alpine
    WORKDIR /app
    RUN addgroup -S apiflow && adduser -S apiflow -G apiflow
    COPY --from=build /app/target/*.jar app.jar
    USER apiflow
    EXPOSE 8081
    ENTRYPOINT ["java", "-jar", "app.jar"]