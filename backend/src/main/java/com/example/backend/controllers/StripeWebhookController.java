package com.example.backend.controllers;

import com.example.backend.entity.*;
import com.example.backend.repository.PaymentRepository;
import com.example.backend.repository.SubscriptionRepository;
import com.example.backend.repository.UserRepository;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionRetrieveParams;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class StripeWebhookController {

    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PaymentRepository paymentRepository;

    @Value("${stripe.webhook.secret}")
    private String endpointSecret;

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader
    ) throws StripeException {

        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, endpointSecret);
        } catch (SignatureVerificationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("");
        }

        switch (event.getType()) {
            case "checkout.session.completed":
                Session sessionPartial = (Session) event.getDataObjectDeserializer().getObject().orElse(null);
                if(sessionPartial != null) handleCheckoutSession(sessionPartial);
                break;

            case "invoice.payment_failed":
                // notifier l'utilisateur
                break;

            case "customer.subscription.updated":
                // mettre à jour le status si nécessaire
                break;
        }

        return ResponseEntity.ok("");
    }


    @Transactional
    public void handleCheckoutSession(Session sessionPartial) throws StripeException {
        // Récupérer la session complète avec line_items
        SessionRetrieveParams params = SessionRetrieveParams.builder()
                .addExpand("line_items.data.price")
                .build();

        Session session = Session.retrieve(sessionPartial.getId(), params, null);
        System.out.println("Stripe Customer ID reçu : " + session.getCustomer());

        // Récupérer l'utilisateur
        User user = userRepository.findByStripeCustomerId(session.getCustomer())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé pour ce Stripe Customer ID !"));

        // Créer la subscription
        Subscription sub = new Subscription();
        sub.setUser(user);
        sub.setSubscriptionStatus(SubscriptionStatus.ACTIVE);

        LocalDate today = LocalDate.now();
        sub.setStartDate(Date.from(today.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        sub.setEndDate(Date.from(today.plusMonths(1).atStartOfDay(ZoneId.systemDefault()).toInstant()));
        subscriptionRepository.save(sub);
        System.out.println("Subscription saved pour user : " + user.getEmail());

        // Créer le paiement
        Payment payment = new Payment();
        payment.setSubscription(sub);
        payment.setPaymentStatus(PaymentStatus.PAID);

        long amountCents = session.getLineItems().getData().get(0).getPrice().getUnitAmount();
        payment.setAmount(amountCents / 100.0);
        payment.setDate(new Date());
        paymentRepository.save(payment);
        System.out.println("Payment saved : " + payment.getAmount() + "€ pour subscription ID : " + sub.getId());
    }
}
