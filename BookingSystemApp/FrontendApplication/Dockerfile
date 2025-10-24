# Use the lightweight official OpenJDK 21 image as the base
FROM openjdk:21-jdk-slim

# Create an argument so Docker knows which jar file to copy
ARG JAR_FILE=target/*.jar

# Copy the jar from the Maven output folder into the container
COPY ${JAR_FILE} app.jar

# Expose the port
EXPOSE 8080

# How the container should start the app
ENTRYPOINT ["java","-jar","/app.jar"]