# 第一阶段：构建 Spring Boot 可执行 jar
FROM maven:3 AS builder

WORKDIR /app

COPY pom.xml ./
COPY src ./src

RUN mvn -B clean package -DskipTests

# 第二阶段：运行后端服务
FROM eclipse-temurin:latest

WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
