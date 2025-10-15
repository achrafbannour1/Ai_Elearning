package com.example.backend.services;


import com.example.backend.entity.Subscription;
import com.example.backend.entity.SubscriptionStatus;
import com.example.backend.entity.User;
import com.example.backend.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SubscriptionService {
    private final SubscriptionRepository subscriptionRepository;


    public Subscription createSubscription(User user, Date startDate, Date endDate) {
        Subscription subscription = new Subscription();
        subscription.setUser(user);
        subscription.setStartDate(startDate);
        subscription.setEndDate(endDate);
        subscription.setSubscriptionStatus(SubscriptionStatus.PENDING);
        return subscriptionRepository.save(subscription);
    }

    public Optional<Subscription> getSubscriptionByUser(User user) {
        return subscriptionRepository.findByUser(user);
    }

    public Subscription updateStatus(Subscription subscription, SubscriptionStatus status) {
        subscription.setSubscriptionStatus(status);
        return subscriptionRepository.save(subscription);
    }
    public Optional<Subscription> getSubscriptionById(Long id) {
        return subscriptionRepository.findById(id);
    }




}
