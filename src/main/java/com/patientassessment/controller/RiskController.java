package com.patientassessment.controller;


import com.patientassessment.model.PatientDetails;
import com.patientassessment.service.RiskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/assess")
public class RiskController {

    private WebClient webClient;

    @Autowired
    private RiskService riskService;

    @GetMapping("/id")
    public Mono<PatientDetails> getRisk(@RequestParam Integer patId) {
        return riskService.getRiskData(patId);
    }



}
