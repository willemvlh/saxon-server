FROM openjdk:8-jdk-alpine
ADD ./target/*.jar saxon.jar
EXPOSE 5000
CMD java -jar saxon.jar
