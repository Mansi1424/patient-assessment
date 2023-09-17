FROM openjdk:17-alpine
WORKDIR /opt
ENV PORT 8080
EXPOSE 8080
COPY target/patient-assessment.jar patient-assessment.jar
COPY src/main/resources/static /app/static
CMD ["java", "-jar", "patient-assessment.jar"]