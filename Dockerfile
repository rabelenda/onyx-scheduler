FROM dockerfile/java:oracle-java8

WORKDIR /opt/onyx-scheduler

COPY target/onyx-scheduler.jar /opt/onyx-scheduler/onyx-scheduler.jar

EXPOSE 8080
CMD ["java", "-jar", "onyx-scheduler.jar"]
