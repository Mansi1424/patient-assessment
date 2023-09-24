package com.patientassessment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.patientassessment.service.RiskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RiskController.class)
public class RiskControllerTest {


    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RiskService riskService;

    @Autowired
    private MockMvc mockMvc;


    @Test
    public void testAssessPatientByID() throws Exception {

        String patId = "123";
        mockMvc.perform(MockMvcRequestBuilders.post("/assess/id")
                        .param("patId", patId))
                .andExpect(status().isOk());

    }


}
