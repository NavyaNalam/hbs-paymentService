package com.navya.hotelpaymentservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Role;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("api/v1/payment")
public class PaymentRestController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentRestController.class);
    @Autowired
    private PaymentRepository paymentRepository;

    @GetMapping("fetch")
    public ResponseEntity<?> fetchPayment(@RequestParam Long paymentId) {
        logger.debug("Fetching payment with ID: " + paymentId);
        Optional<Payment> payment = paymentRepository.findPaymentById(paymentId);
        if (payment.isPresent()) {
            logger.debug("Payment found: " + payment.get());
            return ResponseEntity.ok(payment.get());
        } else {
            logger.debug("Payment not found for ID: " + paymentId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Payment with ID: " + paymentId + " not found");
        }
    }

    @PostMapping("add")
    public ResponseEntity<?> addPayment(@RequestBody Payment payment) {
        logger.debug("Adding payment: " + payment);
        Optional<Payment> existingPayment = paymentRepository.findbyBookingId(payment.getBookingId());
        if (existingPayment.isPresent()) {
            logger.debug("Payment for booking ID " + payment.getBookingId() + " already exists");
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Payment with booking ID " + payment.getBookingId() + " already exists");
        }else {
            logger.debug("Saving new payment with booking ID " + payment.getBookingId());
            paymentRepository.save(payment);
            return ResponseEntity.status(HttpStatus.CREATED).body("Payment added successfully");
        }
    }

    @PutMapping("edit")
    public ResponseEntity<?> editPayment(@RequestBody Payment payment) {
        logger.debug("Editing payment: " + payment);
        Optional<Payment> existingPayment = paymentRepository.findPaymentById(payment.getPaymentId());
        if (existingPayment.isEmpty()) {
            logger.debug("Payment with ID " + payment.getPaymentId() + " does not exist");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Payment with ID " + payment.getPaymentId() + " does not exist");
        } else {
            logger.debug("Updating payment with ID " + payment.getPaymentId());
            paymentRepository.save(payment);
            return ResponseEntity.ok("Payment updated successfully");
        }
    }

    @DeleteMapping("delete")
    public ResponseEntity<?> deletePayment(@RequestParam Long paymentId) {
        logger.debug("Deleting payment with ID: " + paymentId);
        Optional<Payment> existingPayment = paymentRepository.findPaymentById(paymentId);
        if (existingPayment.isPresent()) {
            logger.debug("Deleting payment: " + existingPayment.get());
            paymentRepository.delete(existingPayment.get());
            return ResponseEntity.ok("Payment deleted successfully");
        } else {
            logger.debug("Payment with ID " + paymentId + " does not exist");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Payment with ID " + paymentId + " does not exist");
        }
    }


}