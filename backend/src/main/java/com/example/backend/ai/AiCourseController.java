package com.example.backend.ai;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ai/courses")
@CrossOrigin(origins = "http://localhost:4200") // ajuste selon ton domaine
public class AiCourseController {

    private final SyllabusService service;

    public AiCourseController(SyllabusService service) {
        this.service = service;
    }

    @PostMapping("/syllabus")
    public SyllabusResponse generate(@RequestBody SyllabusRequest req) {
        return service.generate(req);
    }
}
