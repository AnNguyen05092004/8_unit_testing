FROM maven:3.8-openjdk-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests


FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY --from=build /app/target/webtoeic-0.0.1-SNAPSHOT.jar webtoeic.jar
EXPOSE 8888
CMD ["java", "-jar", "webtoeic.jar", "--server.port=8888"]