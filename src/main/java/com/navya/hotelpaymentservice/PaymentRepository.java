package com.navya.hotelpaymentservice;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

public interface PaymentRepository extends MongoRepository<Payment, String> {

    Optional<Payment> findPaymentById(Long id);
    Optional<Payment> findbyBookingId(Long bookingId);
}
