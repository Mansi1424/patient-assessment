package com.patientassessment.service;

import com.patientassessment.model.Patient;
import com.patientassessment.model.PatientDetails;
import com.patientassessment.model.PatientHistory;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class RiskServiceImpl implements RiskService {

    private final WebClient webClientPatientService;
    private final WebClient webClientPatientHistoryService;

    public RiskServiceImpl(WebClient.Builder webClientBuilder,
                           @Value("${patient.service.base-url}") String patientServiceBaseUrl,
                           @Value("${patient.history.service.base-url}") String patientHistoryServiceBaseUrl) {
        this.webClientPatientService = webClientBuilder.baseUrl(patientServiceBaseUrl).build();
        this.webClientPatientHistoryService = webClientBuilder.baseUrl(patientHistoryServiceBaseUrl).build();
    }


    @Override
    public Mono<PatientDetails> getRiskData(Integer patId) {
        Mono<Patient> patientMono = webClientPatientService
                .get()
                .uri("/patient/get/{id}", patId)
                .retrieve()
                .bodyToMono(Patient.class);

        Mono<List<PatientHistory>> historyMono = webClientPatientHistoryService
                .get()
                .uri("/patHistory/getById?patId={patId}", patId)
                .retrieve()
                .bodyToFlux(PatientHistory.class)
                .collectList();

        // Combine the results of both Monos using zip
        return Mono.zip(patientMono, historyMono)
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
                .timeout(Duration.ofSeconds(50))
                .onErrorMap(TimeoutException.class, e -> new RuntimeException("Timeout occurred", e));
    }


    @Override
    public Mono<PatientDetails> getRiskDataByFamilyName(String familyName) {
        Mono<Patient> patientMono = webClientPatientService
                .get()
                .uri("/patient/getByFamily/{familyName}", familyName)
                .retrieve()
                .bodyToMono(Patient.class);

        // Combine the results of both Monos using flatMap
        return patientMono.flatMap(patient -> {
                    // Now that we have the patient object, use its data to make the second API call
                    Mono<List<PatientHistory>> historyMono = webClientPatientHistoryService
                            .get()
                            .uri("/patHistory/getById?patId={patId}", patient.getId()) // Replace patId with the correct field from the patient object
                            .retrieve()
                            .bodyToFlux(PatientHistory.class)
                            .collectList();

                    return historyMono.map(patientHistoryList -> {
                        // Create a PatientDetails object combining data from both APIs
                        PatientDetails response = new PatientDetails();
                        response.setGender(patient.getSex());
                        response.setAge(calculateAge(patient.getDateOfBirth()));

                        // Extracting each note from the patientHistory API response and saving it to a list of notes
                        List<String> notes = new ArrayList<>();
                        for (PatientHistory patientHistory : patientHistoryList) {
                            notes.add(patientHistory.getNote());
                        }

                        response.setNotes(notes);

                        return response;
                    });
                })
                .timeout(Duration.ofSeconds(50))
                .onErrorMap(TimeoutException.class, e -> new RuntimeException("Timeout occurred", e));
    }


    /**
     * Method getRisk - To calculate the risk based off the riskData
     *
     * @param riskData the data from both services to calculate risk
     * @return the risk
     */
    @Override
    public Mono<String> getRisk(Mono<PatientDetails> riskData) {
        return riskData.map(patientDetails -> {
            String risk = "";
            List<String> notes = patientDetails.getNotes();
            int triggerWordsCount = returnTriggerWordsInAllNotes(notes);

            int age = Integer.parseInt(patientDetails.getAge());
            String gender = patientDetails.getGender();
            boolean male = Objects.equals(gender, "M");
            boolean female = Objects.equals(gender, "F");

            if (male && age < 30 && triggerWordsCount == 3) {
                risk = "In Danger";
            } else if (female && age < 30 && triggerWordsCount == 4) {
                risk = "In Danger";
            } else if (age > 30 && triggerWordsCount == 6) {
                risk = "In Danger";
            } else if (age > 30 && triggerWordsCount == 2) {
                risk = "Borderline";
            } else if (age < 30 && male && triggerWordsCount == 5) {
                risk = "Early Onset";
            } else if (age < 30 && female && triggerWordsCount >= 7) {
                risk = "Early Onset";
            } else if (age > 30 && triggerWordsCount >= 8) {
                risk = "Early Onset";
            } else if (triggerWordsCount == 0) {
                risk = "None";
            }


            return risk;

        });


    }

    @Override
    public Mono<String> returnRisk(Integer patId) {
        return getRisk(getRiskData(patId));
    }


    @Override
    public Mono<String> returnRiskByFamilyName(String familyName) {
        return getRisk(getRiskDataByFamilyName(familyName));
    }

    @Override
    public int returnNumTriggerWords(String notes) {
        List<String> triggerWords = Arrays.asList("Hemoglobin A1C", "Microalbumin", "Body Height", "Body Weight", "Smoker", "Abnormal", "Cholesterol", "Dizziness", "Relapse", "Reaction", "Antibodies");
        // Initialize a total count variable
        int totalWordCount = 0;

        // Iterate through the list of words and count their occurrences
        for (String triggerWordToFind : triggerWords) {
            Pattern pattern = Pattern.compile("\\b" + triggerWordToFind + "\\b", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(notes);

            while (matcher.find()) {
                totalWordCount++;
            }
        }

        return totalWordCount;
    }

    @Override
    public int returnTriggerWordsInAllNotes(@NotNull List<String> notesList) {
        int totalTriggerWordsCount = 0;
        for (String note : notesList) {
            int triggerWordsCount = returnNumTriggerWords(note);
            totalTriggerWordsCount += triggerWordsCount;
        }
        return totalTriggerWordsCount;
    }


    @Override
    public @NotNull String calculateAge(String dob) {
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
