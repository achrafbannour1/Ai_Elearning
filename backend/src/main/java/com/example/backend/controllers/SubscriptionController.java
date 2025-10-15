package com.example.backend.controllers;


import com.example.backend.entity.Subscription;
import com.example.backend.entity.SubscriptionStatus;
import com.example.backend.entity.User;
import com.example.backend.repository.UserRepository;
import com.example.backend.services.SubscriptionService;
import com.example.backend.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.Optional;

@RestController
@RequestMapping("/api/subscription")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;
    private final UserService userService;

    /**
     * Créer un abonnement pour l'utilisateur connecté
     */
    @PostMapping("/createSubscription")
    public Subscription createSubscription(Authentication authentication) {
        User user = userService.getUserFromAuth(authentication);
        Date startDate = new Date();
        Date endDate = new Date(System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000); // +30 jours
        return subscriptionService.createSubscription(user, startDate, endDate);
    }

    /**
     * Récupérer l'abonnement de l'utilisateur connecté
     */
    @GetMapping("/getMySubscription")
    public Optional<Subscription> getMySubscription(Authentication authentication) {
        User user = userService.getUserFromAuth(authentication);
        return subscriptionService.getSubscriptionByUser(user);
    }

    /**
     * Récupérer l'abonnement par userId (ADMIN uniquement)
     */


    @GetMapping("/getSubscriptionByUserId/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public Optional<Subscription> getSubscriptionByUserId(@PathVariable Long userId) {
        User user = new User();
        user.setId(userId);
        return subscriptionService.getSubscriptionByUser(user);
    }

    /**
     * Mettre à jour le statut d'un abonnement (ADMIN uniquement)
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public Subscription updateStatus(@PathVariable Long id, @RequestParam SubscriptionStatus status) {
        Optional<Subscription> sub = subscriptionService.getSubscriptionById(id);
        return sub.map(subscription -> subscriptionService.updateStatus(subscription, status)).orElse(null);
    }

}
