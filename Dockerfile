FROM openjdk:14-jdk-alpine
VOLUME /tmp
COPY target/smsreceiver-0.0.1.jar app.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]
