package com.example.backend.controllers;


import com.example.backend.DTO.LoginRequest;
import com.example.backend.DTO.LoginResponse;
import com.example.backend.DTO.RegisterRequest;
import com.example.backend.entity.Role;
import com.example.backend.entity.User;
import com.example.backend.repository.UserRepository;
import com.example.backend.security.JwtUtil;
import com.example.backend.services.RecaptchaService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    @Autowired
    private RecaptchaService recaptchaService;
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest login) {

        // Vérifier le reCAPTCHA
       /* boolean captchaVerified = recaptchaService.verifyToken(login.getRecaptchaToken());
        if (!captchaVerified) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("reCAPTCHA invalide");
        }*/

        try {
            // Authentification
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(login.getEmail(), login.getPassword())
            );

            // Récupération de l'utilisateur
            User user = userRepository.findByEmail(login.getEmail())
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            // Génération du token JWT
            String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());

            // Retourner le token + info utilisateur directement
            return ResponseEntity.ok(new LoginResponse(token, user));

        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Email ou mot de passe incorrect");
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.ROLE_STUDENT);
        user = userRepository.saveAndFlush(user); // Force flush to DB
        System.out.println("Registered user: " + user.getEmail() + ", ID: " + user.getId());
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
        return ResponseEntity.ok(new LoginResponse(token, user));
    }


}
