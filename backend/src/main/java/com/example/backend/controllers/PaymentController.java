package com.example.backend.controllers;


import com.example.backend.entity.Payment;
import com.example.backend.entity.PaymentMethod;
import com.example.backend.entity.PaymentStatus;
import com.example.backend.entity.Subscription;
import com.example.backend.services.PaymentService;
import com.example.backend.services.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final SubscriptionService subscriptionService;

    /**
     * Créer un paiement pour une subscription existante
     */
    @PostMapping("/createPayment/{subscriptionId}")
    public Payment createPayment(@PathVariable Long subscriptionId,
                                 @RequestParam double amount,
                                 @RequestParam PaymentMethod method) {

        Optional<Subscription> subscription = subscriptionService.getSubscriptionById(subscriptionId);
        return subscription.map(sub -> paymentService.createPayment(sub, amount, method)).orElse(null);
    }

    /**
     * Récupérer tous les paiements d'une subscription
     */
    @GetMapping("/getPaymentsBySubscription/{subscriptionId}")
    public List<Payment> getPaymentsBySubscription(@PathVariable Long subscriptionId) {
        Optional<Subscription> subscription = subscriptionService.getSubscriptionById(subscriptionId);
        return subscription.map(paymentService::getPaymentsBySubscription).orElse(List.of());
    }

    /**
     * Mettre à jour le statut d'un paiement (ADMIN uniquement)
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public Payment updateStatus(@PathVariable Long id, @RequestParam PaymentStatus status) {
        Optional<Payment> payment = paymentService.getPaymentById(id);
        return payment.map(p -> paymentService.updateStatus(p, status)).orElse(null);
    }

}
