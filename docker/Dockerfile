# Use the official OpenJDK image as the base image
FROM openjdk:1.8

# Set the working directory inside the container
WORKDIR /usr/src/app

# Copy the Java executable JAR file into the container
COPY ../classes/LoadTester.jar .

ENV FREQUENCY 10
ENV TARGET http://10.152.183.222:30000/primecheck

# Define the command to run your application
CMD ["java", "-jar", "LoadTester.jar", "${FREQUENCY}", "${TARGET}"]



