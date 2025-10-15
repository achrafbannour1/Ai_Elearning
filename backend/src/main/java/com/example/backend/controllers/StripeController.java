package com.example.backend.controllers;


import com.example.backend.entity.User;
import com.example.backend.repository.UserRepository;
import com.example.backend.services.StripeService;
import com.stripe.exception.StripeException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/stripe")
@RequiredArgsConstructor
public class StripeController {



    private final UserRepository userRepository;
    private final StripeService stripeService;

    /**
     * Créer un Stripe Customer pour un utilisateur
     */
    @PostMapping("/create-customer/{userId}")
    public ResponseEntity<String> createCustomer(@PathVariable Long userId) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) return ResponseEntity.notFound().build();

            String customerId = stripeService.createStripeCustomer(user);
            return ResponseEntity.ok(customerId);

        } catch (StripeException e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    /**
     * Créer une Checkout Session pour un abonnement
     */
    @PostMapping("/create-checkout-session")
    public ResponseEntity<String> createCheckoutSession(
            @RequestParam String email,
            @RequestParam String priceId
    ) {
        Optional<User> optUser = userRepository.findByEmail(email);
        if (optUser.isEmpty()) {
            System.out.println("Utilisateur non trouvé pour email: " + email);
            return ResponseEntity.notFound().build();
        }

        User user = optUser.get();
        System.out.println("Utilisateur trouvé: " + user.getEmail());

        try {
            String url = stripeService.createCheckoutSession(user, priceId);
            System.out.println("Stripe Checkout URL: " + url);
            return ResponseEntity.ok(url);
        } catch (StripeException e) {
            e.printStackTrace(); // affiche l'erreur complète
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

}
