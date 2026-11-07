# ---- Build stage: compile and package the Spring Boot app ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B -q package -DskipTests

# ---- Run stage: small Java runtime image ----
FROM eclipse-temurin:21-jre
WORKDIR /app
RUN useradd --system --uid 1001 shopycat && mkdir -p /app/data && chown shopycat /app/data
COPY --from=build /app/target/shopycat-*.jar app.jar
USER shopycat
ENV PORT=8080
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT} -jar app.jar"]
