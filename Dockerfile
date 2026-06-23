FROM maven:3.9-eclipse-temurin-25 AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests -Dspotless.check.skip=true -B

FROM eclipse-temurin:25-jdk
WORKDIR /app
COPY --from=builder /app/target/points-mall-thirdparty-connector-*.jar app.jar
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "app.jar"]
