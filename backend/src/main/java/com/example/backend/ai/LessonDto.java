package com.example.backend.ai;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter @AllArgsConstructor
public class LessonDto {
    private String title;
    private List<String> outcomes;
}
