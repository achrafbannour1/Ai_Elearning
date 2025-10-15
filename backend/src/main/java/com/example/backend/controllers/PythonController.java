package com.example.backend.controllers;


import com.example.backend.services.PythonService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class PythonController {


    @Autowired
    private  PythonService pythonService;



    @GetMapping("/revenus")
    public Map<String, Object> getRevenus() throws IOException, InterruptedException {
        // 1️⃣ Générer le JSON avec Python à la volée
        pythonService.generateForecast();

        // 2️⃣ Lire le JSON généré
        ObjectMapper mapper = new ObjectMapper();
        File file = new File("C:/Users/LENOVO/Desktop/projet web IA/Ai_Elearning/stripe_IA/revenus.json");
        return mapper.readValue(file, Map.class);
    }
}
