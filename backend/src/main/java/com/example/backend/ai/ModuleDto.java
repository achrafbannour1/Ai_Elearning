package com.example.backend.ai;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter @AllArgsConstructor
public class ModuleDto {
    private String title;
    private List<String> objectives;
    private List<LessonDto> lessons;
    private List<String> exercises;
}
