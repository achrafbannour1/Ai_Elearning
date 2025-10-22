package com.example.backend.ai;

import org.springframework.stereotype.Service;
import java.io.*;

@Service
public class AiLocalService {

    public String summarize(String content) {
        try {
            // Lancer le script Python (situé à la racine du backend)
            ProcessBuilder pb = new ProcessBuilder("python", "ai_tool.py");
            pb.directory(new File(System.getProperty("user.dir"))); // Exécution depuis le dossier backend
            Process process = pb.start();

            // Envoyer le texte à Python via stdin
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()))) {
                writer.write(content);
            }

            // Lire la sortie (résumé)
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line).append("\n");
            }

            process.waitFor();
            return result.toString().trim();
        } catch (Exception e) {
            e.printStackTrace();
            return "Erreur AI: " + e.getMessage();
        }
    }
}
