package com.example.backend.ai;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class SyllabusRequest {
    private String title;     // ex: "Spring Boot & Angular 16"
    private String level;     // debutant | intermediaire | avance
    private int duration;     // en semaines
    private String audience;  // ex: "Étudiants 3e année GL"
}
