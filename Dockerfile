FROM openjdk:17-alpine
WORKDIR /opt
ENV PORT 8080
EXPOSE 8080
COPY target/assessment.jar assessment.jar
CMD ["java", "-jar", "/assessment.jar"]