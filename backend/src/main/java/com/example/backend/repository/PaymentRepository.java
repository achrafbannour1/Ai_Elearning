package com.example.backend.repository;

import com.example.backend.entity.Payment;
import com.example.backend.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findBySubscription(Subscription subscription);

}
