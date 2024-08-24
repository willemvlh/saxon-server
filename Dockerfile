FROM eclipse-temurin:17
RUN mkdir opt/app
ADD ./target/*.jar opt/app/saxon.jar
COPY ./docker/entrypoint.sh /opt/app/entrypoint.sh
RUN chmod +x /opt/app/entrypoint.sh
EXPOSE 5000
ENTRYPOINT ["/opt/app/entrypoint.sh"]