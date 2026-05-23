# 运行阶段：Actions 预先构建 jar，容器只负责启动服务
FROM eclipse-temurin:latest

WORKDIR /app

COPY app.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
