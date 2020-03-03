FROM java:8-jdk-alpine
ADD ./target/saxon-1.1.jar saxon.jar
EXPOSE 5000
CMD java -jar saxon.jar
