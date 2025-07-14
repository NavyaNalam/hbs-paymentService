package com.navya.hotelpaymentservice;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface PaymentRepository extends MongoRepository<Payment, String> {

    Optional<Payment> findPaymentByPaymentId(String paymentId);

    Optional<Payment> findPaymentByBookingId(String bookingId);
}
