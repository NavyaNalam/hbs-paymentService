package com.navya.hotelpaymentservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Role;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.LocalDate;
import java.util.Optional;


@RestController
@RequestMapping("api/v1/payment")
public class PaymentRestController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentRestController.class);

    @Autowired
    private PaymentRepository paymentRepo;

    @Autowired
    private PaymentEventProducer paymentEventProducer;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    TokenService tokenService;


    @GetMapping("fetch/{userId}/{paymentId}")
    public ResponseEntity<?> fetchPayment(@RequestParam String paymentId, @RequestHeader("Authorization") String token, @RequestParam String userId) {
        String phone = null;
        try
        {
            phone =  tokenService.validateToken(token);
        }
        catch (WebClientResponseException e)
        {
            logger.info("Token validation failed: " + e.getMessage());
            return ResponseEntity.status(401).body("Invalid token");
        }

        logger.info("Phone number from token: " + phone);
        if(!phone.equals(userId))
        {
            logger.info("Phone number mismatch");
            return ResponseEntity.status(401).body("Invalid token or phone number mismatch");
        }

        logger.debug("Fetching payment with ID: " + paymentId);
        Optional<Payment> payment = paymentRepo.findPaymentByPaymentId(paymentId);
        if (payment.isPresent()) {
            logger.debug("Payment found: " + payment.get());
            return ResponseEntity.ok(payment.get());
        } else {
            logger.debug("Payment not found for ID: " + paymentId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Payment with ID: " + paymentId + " not found");
        }
    }

    @PostMapping("addPayment/{userId}")
    public ResponseEntity<?> addPayment(@RequestBody Payment payment, @RequestHeader("Authorization") String token, @RequestParam String userId) {
        String phone = null;
        try
        {
            phone =  tokenService.validateToken(token);
        }
        catch (WebClientResponseException e)
        {
            logger.info("Token validation failed: " + e.getMessage());
            return ResponseEntity.status(401).body("Invalid token");
        }

        logger.info("Phone number from token: " + phone);
        if(!phone.equals(userId))
        {
            logger.info("Phone number mismatch");
            return ResponseEntity.status(401).body("Invalid token or phone number mismatch");
        }

        logger.debug("Adding payment: " + payment);
        Optional<Payment> existingPayment = paymentRepo.findBookingByBookingId(payment.getBookingId());
        if (existingPayment.isPresent()) {
            logger.debug("Payment for booking ID " + payment.getBookingId() + " already exists");
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Payment with booking ID " + payment.getBookingId() + " already exists");
        }else {
            logger.debug("Saving new payment with booking ID " + payment.getBookingId());
            payment.setDateofPayment(LocalDate.now());
            paymentRepo.save(payment);

            // Publish the payment event
            logger.info("Proceeding to publish the payment event for booking ID: {}", payment.getBookingId());
            PaymentEvent paymentEvent = new PaymentEvent();
            paymentEvent.setBookingId(payment.getBookingId());
            paymentEvent.setStatus("SUCCESS");
            paymentEvent.setPaymentId(payment.getPaymentId());
            paymentEvent.setTotalFare(payment.getTotalPrice());
            redisTemplate.opsForValue().set(paymentEvent.getBookingId().toString(), "PAYMENT SUCCEEDED");
            paymentEventProducer.publishEvent(paymentEvent);
            return ResponseEntity.status(HttpStatus.CREATED).body("Payment added successfully. Please wait for confirmation of Your Booking.");
        }
    }

    @PutMapping("edit")
    public ResponseEntity<?> editPayment(@RequestBody Payment payment) {
        logger.debug("Editing payment: " + payment);
        Optional<Payment> existingPayment = paymentRepo.findPaymentByPaymentId(payment.getPaymentId());
        if (existingPayment.isEmpty()) {
            logger.debug("Payment with ID " + payment.getPaymentId() + " does not exist");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Payment with ID " + payment.getPaymentId() + " does not exist");
        } else {
            logger.debug("Updating payment with ID " + payment.getPaymentId());
            paymentRepo.save(payment);
            return ResponseEntity.ok("Payment updated successfully");
        }
    }

    @DeleteMapping("delete")
    public ResponseEntity<?> deletePayment(@RequestParam String paymentId) {
        logger.debug("Deleting payment with ID: " + paymentId);
        Optional<Payment> existingPayment = paymentRepo.findPaymentByPaymentId(paymentId);
        if (existingPayment.isPresent()) {
            logger.debug("Deleting payment: " + existingPayment.get());
            paymentRepo.delete(existingPayment.get());
            return ResponseEntity.ok("Payment deleted successfully");
        } else {
            logger.debug("Payment with ID " + paymentId + " does not exist");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Payment with ID " + paymentId + " does not exist");
        }
    }


}