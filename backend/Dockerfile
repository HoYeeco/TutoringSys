# 构建阶段
FROM maven:3.8.4-openjdk-17-slim AS builder
WORKDIR /app
COPY pom.xml .
COPY tutoringsys-common/pom.xml tutoringsys-common/
COPY tutoringsys-dao/pom.xml tutoringsys-dao/
COPY tutoringsys-service/pom.xml tutoringsys-service/
COPY tutoringsys-security/pom.xml tutoringsys-security/
COPY tutoringsys-llm/pom.xml tutoringsys-llm/
COPY tutoringsys-api/pom.xml tutoringsys-api/
RUN mvn dependency:go-offline
COPY . .
RUN mvn clean package -DskipTests

# 运行阶段
FROM openjdk:17-jre-slim
WORKDIR /app
COPY --from=builder /app/tutoringsys-api/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
