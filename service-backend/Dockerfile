# Create image based on the official openjdk image from dockerhub
# BUILD
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /usr/src/compile
COPY . .
CMD ["mvn", "clean", "package", "-DskipTests"]


# RUN
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /usr/src/service-backend
# The application's jar file
ARG JAR_FILE=target/*.jar
COPY --from=build /usr/src/compile/${JAR_FILE} app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "./app.jar"]