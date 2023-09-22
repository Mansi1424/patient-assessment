package com.patientassessment.service;

import com.patientassessment.model.PatientDetails;
import reactor.core.publisher.Mono;

import java.util.List;

public interface RiskService {
    Mono<PatientDetails> getRiskData(Integer patId);

    Mono<String> getRisk(Mono<PatientDetails> riskData);

    int returnNumTriggerWords(String notes);
    int returnTriggerWordsInAllNotes(List<String> notesList);

    String calculateAge(String dob);

    Mono<String> returnRisk(Integer patId);

}
