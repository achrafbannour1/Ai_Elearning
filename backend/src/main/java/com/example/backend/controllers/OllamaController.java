package com.example.backend.controllers;

import com.example.backend.services.OllamaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/ollama")
public class OllamaController {
    @Autowired
    private OllamaService ollamaService;

    @PostMapping("/query")
    public Map<String, Object> queryOllama(@RequestBody Map<String, String> request) {
        String userQuery = request.get("query");
        return ollamaService.processQuery(userQuery);
    }
}