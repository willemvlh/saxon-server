FROM eclipse-temurin:17
RUN mkdir opt/app
ADD ./target/*.jar opt/app/saxon.jar
EXPOSE 5000
CMD java $JAVA_OPTS -jar  opt/app/saxon.jar