# ---- build ----
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build

# Baixa as dependencias numa camada separada, para o cache sobreviver a mudanca de codigo
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
RUN mvn -B -q clean package -DskipTests

# ---- runtime ----
# jammy cobre amd64 e arm64 (o alpine nao tem build arm64)
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

RUN groupadd --system app && useradd --system --gid app app
COPY --from=build /build/target/*.jar app.jar
USER app

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
