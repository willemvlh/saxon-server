FROM eclipse-temurin:25-jre-alpine
COPY target/saxon-server-*.jar app.jar
EXPOSE 5000
ENV JAVA_OPTS=""
CMD java $JAVA_OPTS -jar app.jar
