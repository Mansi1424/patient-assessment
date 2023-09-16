package com.patientassessment.service;

import com.patientassessment.model.Patient;
import com.patientassessment.model.PatientDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeoutException;

@Service
public class RiskServiceImpl implements RiskService{

    private WebClient webClient;

    public RiskServiceImpl(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://localhost:8081").build();
    }
    @Override
    public Mono<PatientDetails> getRisk(Integer patId) {
        return webClient.get()
                .uri("/patient/get/{id}", patId)
                .retrieve()
                .bodyToMono(Patient.class)
                .map(patient -> {
                    PatientDetails response = new PatientDetails();
                    response.setAge(calculateAge(patient.getDateOfBirth()));
                    response.setGender(patient.getSex());
                    return response;
                })
                .timeout(Duration.ofSeconds(10))
                .onErrorMap(TimeoutException.class, e -> new RuntimeException("Timeout occured", e));

    }

    public static String calculateAge(String dob) {
        // Define the date format for the DOB string
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        try {
            // Parse the DOB string into a LocalDate object
            LocalDate birthDate = LocalDate.parse(dob, formatter);

            // Get the current date
            LocalDate currentDate = LocalDate.now();

            // Calculate the age
            Period period = Period.between(birthDate, currentDate);


            return String.valueOf(period.getYears());
        } catch (Exception e) {
            // Handle parsing errors or other exceptions here
            e.printStackTrace();
            return "Invalid DOB format or calculation error";
        }
    }


}
