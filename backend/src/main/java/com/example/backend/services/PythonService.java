package com.example.backend.services;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@Service
@RequiredArgsConstructor
public class PythonService {


    public void generateForecast() throws IOException, InterruptedException {
        // Chemin vers l’exécutable Python
        String pythonExe = "C:/Users/LENOVO/AppData/Local/Programs/Python/Python314/python.exe";
        // Chemin vers ton script Python
        String scriptPath = "C:/Users/LENOVO/Desktop/projet web IA/Ai_Elearning/stripe_IA/stripe_test.py";

        ProcessBuilder pb = new ProcessBuilder(pythonExe, scriptPath);
        pb.redirectErrorStream(true);
        Process process = pb.start();

        // Afficher les logs du script dans la console Spring Boot
        BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        while ((line = in.readLine()) != null) {
            System.out.println(line);
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("Erreur lors de l'exécution du script Python");
        }
    }





}
