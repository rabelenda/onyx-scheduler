FROM openjdk:8-jdk-alpine

WORKDIR /opt/onyx-scheduler

COPY target/onyx-scheduler.jar /opt/onyx-scheduler/onyx-scheduler.jar

EXPOSE 28080
CMD ["java", "-jar", "onyx-scheduler.jar"]
