package com.example.backend.services;

import com.example.backend.entity.*;
import com.example.backend.repository.PaymentRepository;
import com.example.backend.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PaymentService {

    @Autowired
    private final PaymentRepository paymentRepository;

    @Autowired
    private final SubscriptionRepository subscriptionRepository;


    public Payment createPayment(Subscription subscription, double amount, PaymentMethod method) {
        Payment payment = new Payment();
        payment.setSubscription(subscription);
        payment.setAmount(amount);
        payment.setDate(new Date());
        payment.setPaymentMethod(method);
        payment.setPaymentStatus(PaymentStatus.PENDING);
        return paymentRepository.save(payment);
    }

    public List<Payment> getPaymentsBySubscription(Subscription subscription) {
        return paymentRepository.findBySubscription(subscription);
    }

    public Payment updateStatus(Payment payment, PaymentStatus status) {
        payment.setPaymentStatus(status);
        Payment savedPayment = paymentRepository.save(payment);

        // ✅ Si le paiement est confirmé, activer la subscription associée
        if (status == PaymentStatus.PAID) {
            Subscription sub = payment.getSubscription();
            if (sub != null) {
                sub.setSubscriptionStatus(SubscriptionStatus.ACTIVE);
                subscriptionRepository.save(sub);
            }
        }

        return savedPayment;
    }





    public Optional<Payment> getPaymentById(Long id) {
        return paymentRepository.findById(id);
    }



}
