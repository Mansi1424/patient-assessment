package com.patientassessment.service;

import com.patientassessment.model.Patient;
import com.patientassessment.model.PatientDetails;
import com.patientassessment.model.Risk;
import reactor.core.publisher.Mono;

public interface RiskService {
    Mono<PatientDetails> getRisk(Integer patId);
}
