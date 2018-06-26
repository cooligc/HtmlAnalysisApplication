FROM openjdk:8-jdk-alpine
RUN mkdir -p /opt/html-analysis
COPY ./target/*.jar /opt/html-analysis
RUN chmod 755 /opt/html-analysis
EXPOSE 8080
CMD [ "java", "-jar","/opt/html-analysis/Scout24App.jar"]
