package com.patientassessment.controller;


import com.patientassessment.model.PatientDetails;
import com.patientassessment.service.RiskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/assess")
public class RiskController {

    private WebClient webClient;

    @Autowired
    private RiskService riskService;

    @GetMapping("/patId")
    public Mono<PatientDetails> getRiskData(@RequestParam Integer patId) {

        return riskService.getRiskData(patId);
    }

//    @GetMapping("/id")
//    public String getRisk(@RequestParam Integer patId) {
//
//        return riskService.getRisk(riskService.getRiskData(patId));
//    }

    @PostMapping("/id")
    public ResponseEntity<Mono<String>> assessPatient(@RequestParam String patId) {

        Mono<String> risk = riskService.returnRisk(Integer.valueOf(patId));
        return ResponseEntity.ok(risk);

    }



}
