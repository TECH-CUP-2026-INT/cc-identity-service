# Etapa 1: compilar
FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /app

COPY mvnw pom.xml ./
COPY .mvn .mvn
RUN chmod +x mvnw && ./mvnw dependency:go-offline -q

COPY src ./src
RUN ./mvnw clean package -DskipTests -q

# Etapa 2: ejecutar
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
