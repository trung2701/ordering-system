# Use an official OpenJDK runtime as a parent image
FROM openjdk:17-jdk-slim

# Set the working directory inside the container
WORKDIR /app

COPY target/coffee-ordering-system.jar /app/coffee-ordering-system.jar

EXPOSE 8080

# Run the JAR file
ENTRYPOINT ["java", "-jar", "/app/coffee-ordering-system.jar"]
