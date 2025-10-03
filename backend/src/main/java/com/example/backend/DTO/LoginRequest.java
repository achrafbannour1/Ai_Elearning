package com.example.backend.DTO;


import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Data

public class LoginRequest {
    private String email;
    private String password;
    private String recaptchaToken;
}
