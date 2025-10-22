package com.example.backend.ai;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter @AllArgsConstructor
public class SyllabusResponse {
    private String title;
    private String level;
    private String audience;
    private int duration;
    private List<ModuleDto> modules;
}
