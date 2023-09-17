package com.patientassessment.service;

import com.patientassessment.model.Patient;
import com.patientassessment.model.PatientDetails;
import com.patientassessment.model.PatientHistory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

@Service
public class RiskServiceImpl implements RiskService{

    private final WebClient webClientPatientService;
    private final WebClient webClientPatientHistoryService;

    public RiskServiceImpl(WebClient.Builder webClientBuilder, WebClient webClientPatientService) {
        this.webClientPatientService = webClientBuilder.baseUrl("http://localhost:8081").build();
        this.webClientPatientHistoryService = webClientBuilder.baseUrl("http://localhost:8082").build();

    }
//    @Override
//    public Mono<PatientDetails> getRisk(Integer patId) {
//        return webClientPatientService.get()
//                .uri("/patient/get/{id}", patId)
//                .retrieve()
//                .bodyToMono(Patient.class)
//                .map(patient -> {
//                    PatientDetails response = new PatientDetails();
//                    response.setAge(calculateAge(patient.getDateOfBirth()));
//                    response.setGender(patient.getSex());
//                    return response;
//                })
//                .timeout(Duration.ofSeconds(10))
//                .onErrorMap(TimeoutException.class, e -> new RuntimeException("Timeout occured", e));
//
//    }

    @Override
    public Mono<PatientDetails> getRiskData(Integer patId) {
        Mono<Patient> riskMono = webClientPatientService
                .get()
                .uri("/patient/get/{id}", patId)
                .retrieve()
                .bodyToMono(Patient.class);

        Mono<List<PatientHistory>> otherDataMono = webClientPatientHistoryService
                .get()
                .uri("/patHistory/getById?patId={patId}", patId)
                .retrieve()
                .bodyToFlux(PatientHistory.class)
                .collectList();

        // Combine the results of both Monos using zip
        return Mono.zip(riskMono, otherDataMono)
                .map(tuple -> {
                    Patient patient = tuple.getT1();
                    List<PatientHistory> patientHistoryList = tuple.getT2();

                    // Create a PatientResponse object combining data from both APIs
                    PatientDetails response = new PatientDetails();
                    response.setGender(patient.getSex());
                    response.setAge(calculateAge(patient.getDateOfBirth()));

                    //Extracting each note from the patientHistory api response and saving it to a list of notes
                    List<String> notes = new ArrayList<>();
                    for (PatientHistory patientHistory : patientHistoryList) {
                        notes.add(patientHistory.getNote());
                    }

                    response.setNotes(notes);

                    return response;
                })
                .timeout(Duration.ofSeconds(10))
                .onErrorMap(TimeoutException.class, e -> new RuntimeException("Timeout occurred", e));
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
