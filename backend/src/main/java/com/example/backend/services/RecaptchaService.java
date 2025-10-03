package com.example.backend.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;



@Service
@RequiredArgsConstructor
public class RecaptchaService {
    private static final String SECRET_KEY = "6LcMqIorAAAAADwYB7z61uCmG8goJobNvsdwbAAU";
    private static final String VERIFY_URL = "https://www.google.com/recaptcha/api/siteverify";

    public boolean verifyToken(String token) {
        RestTemplate restTemplate = new RestTemplate();

        String params = "?secret=" + SECRET_KEY + "&response=" + token;

        Map<String, Object> response = restTemplate.postForObject(VERIFY_URL + params, null, Map.class);

        return (Boolean) response.get("success");
    }
}
