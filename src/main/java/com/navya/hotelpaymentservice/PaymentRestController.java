package com.navya.hotelpaymentservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
    public ResponseEntity<?> fetchPayment(@PathVariable String paymentId, @RequestHeader("Authorization") String token, @PathVariable("userId") String userId) {
        String phone = null;
        try {
            phone = tokenService.validateToken(token);
        } catch (WebClientResponseException e) {
            logger.info("Token validation failed: " + e.getMessage());
            return ResponseEntity.status(401).body("Invalid token");
        }

        logger.info("Phone number from token: " + phone);
        if (!phone.equals(userId)) {
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

    @PostMapping("/add/{condition}")
    public ResponseEntity<?> addPayment(@RequestBody BookingEvent bookingEvent, @RequestHeader("Authorization") String token, @PathVariable("condition") String condition) {
        // UserId is the phone number of the user
        logger.debug("Received request to add payment for userId: " + bookingEvent.getUserId());
        String phone = null;
        try {
            phone = tokenService.validateToken(token);
        } catch (WebClientResponseException e) {
            logger.info("Token validation failed: " + e.getMessage());
            return ResponseEntity.status(401).body("Invalid token");
        }

        logger.info("Phone number from token: " + phone);
        if (!phone.equals(bookingEvent.getUserId())) {
            logger.info("Phone number mismatch");
            return ResponseEntity.status(401).body("Invalid token or phone number mismatch");
        }

        logger.debug("Adding payment for booking Id: " + bookingEvent.getBookingId());
        Optional<Payment> existingPayment = paymentRepo.findPaymentByBookingId(bookingEvent.getBookingId());
        if (existingPayment.isPresent()) {
            if (existingPayment.get().getPaymentStatus().equals("COMPLETED")) {
                logger.debug("Payment for booking ID " + bookingEvent.getBookingId() + " already exists and is completed");
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Payment with booking ID " + bookingEvent.getBookingId() + " already exists and is SUCCEEDED");
            }
            else
            {
                logger.debug("Payment for booking ID " + bookingEvent.getBookingId() + " exists but is not completed, updating payment status");
                Payment payment = existingPayment.get();
                payment.setTotalPrice(bookingEvent.getTotalFare());
                payment.setDateofPayment(LocalDate.now());
                if (condition.equals("SUCCESS")) {
                    logger.info("Processing payment for booking ID: " + bookingEvent.getBookingId());
                    payment.setPaymentStatus("COMPLETED");
                    paymentRepo.save(payment);
                    logger.info("Payment successful for booking ID: " + bookingEvent.getBookingId());

                    // Publish the payment event
                    PaymentEvent paymentEvent = new PaymentEvent();
                    paymentEvent.setBookingId(payment.getBookingId());
                    paymentEvent.setStatus("SUCCESS");
                    paymentEvent.setPaymentId(payment.getPaymentId());
                    paymentEvent.setTotalFare(payment.getTotalPrice());
                    redisTemplate.opsForValue().set(paymentEvent.getBookingId().toString(), "PAYMENT SUCCEEDED");
                    paymentEventProducer.publishEvent(paymentEvent);
                    return ResponseEntity.status(HttpStatus.CREATED).body("Payment updated successfully. Please wait for confirmation of Your Booking.");
                } else {
                    logger.info("Payment failed for booking ID: " + bookingEvent.getBookingId());
                    payment.setPaymentStatus("FAILED");
                    paymentRepo.save(payment);
                    PaymentEvent paymentEvent = new PaymentEvent();
                    paymentEvent.setBookingId(bookingEvent.getBookingId());
                    paymentEvent.setStatus("FAILURE");
                    paymentEvent.setPaymentId(payment.getPaymentId());
                    paymentEvent.setTotalFare(bookingEvent.getTotalFare());
                    redisTemplate.opsForValue().set(paymentEvent.getBookingId().toString(), "PAYMENT FAILED");
                    paymentEventProducer.publishEvent(paymentEvent);
                    return ResponseEntity.status(HttpStatus.CREATED).body("Payment Failed. Please try again.");
                }
            }
        }
        // If no existing payment found, create a new one
        else {
            logger.debug("Saving new payment with booking ID " + bookingEvent.getBookingId());
            Payment payment = new Payment();
            payment.setBookingId(bookingEvent.getBookingId());
            payment.setDateofPayment(LocalDate.now());
            payment.setTotalPrice(bookingEvent.getTotalFare());
            if (condition.equals("SUCCESS")) {
                logger.info("Processing payment for booking ID: " + bookingEvent.getBookingId());
                // Assuming the payment is successful, you can add your payment processing logic here
                // For example, you might call a payment gateway API to process the payment

                payment.setPaymentStatus("COMPLETED");
                paymentRepo.save(payment);
                logger.info("Payment successful for booking ID: " + bookingEvent.getBookingId());

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
            } else {
                logger.info("Payment failed for booking ID: " + bookingEvent.getBookingId());
                payment.setPaymentStatus("FAILED");
                paymentRepo.save(payment);
                PaymentEvent paymentEvent = new PaymentEvent();
                paymentEvent.setBookingId(bookingEvent.getBookingId());
                paymentEvent.setStatus("FAILURE");
                paymentEvent.setPaymentId(payment.getPaymentId());
                paymentEvent.setTotalFare(bookingEvent.getTotalFare());
                redisTemplate.opsForValue().set(paymentEvent.getBookingId().toString(), "PAYMENT FAILED");
                paymentEventProducer.publishEvent(paymentEvent);
                return ResponseEntity.status(HttpStatus.CREATED).body("Payment Failed. Please try again.");
            }
        }
}

    @PutMapping("edit")
    public ResponseEntity<?> editPayment(@RequestBody Payment payment, @RequestHeader("Authorization") String token) {
        String phone = null;
        try {
            phone = tokenService.validateToken(token);
        } catch (WebClientResponseException e) {
            logger.info("Token validation failed: " + e.getMessage());
            return ResponseEntity.status(401).body("Invalid token");
        }
        if (phone.isEmpty()) {
            logger.info("Token validation failed: Phone number is empty");
            return ResponseEntity.status(401).body("Token Not Found");
        }

        String role = tokenService.getRoleFromToken(token);

        if(!role.equals("ADMIN")){
            logger.info("Unauthorized access: User is not an admin");
            return ResponseEntity.status(403).body("Access Denied: Only admins can edit payments");
        }

        logger.debug("Editing payment: " + payment);
        if(redisTemplate.hasKey(payment.getBookingId().toString())) {
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
        else{
            logger.debug("Payment with ID " + payment.getPaymentId() + " does not exist");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Payment with ID " + payment.getPaymentId() + " Could not be fetched, Please try again later");
        }
    }

    @DeleteMapping("delete")
    public ResponseEntity<?> deletePayment(@RequestParam String paymentId, @RequestHeader("Authorization") String token) {
        String phone = null;
        try {
            phone = tokenService.validateToken(token);
        } catch (WebClientResponseException e) {
            logger.info("Token validation failed: " + e.getMessage());
            return ResponseEntity.status(401).body("Invalid token");
        }
        if (phone.isEmpty()) {
            logger.info("Token validation failed: Phone number is empty");
            return ResponseEntity.status(401).body("Token Not Found");
        }

        String role = tokenService.getRoleFromToken(token);

        if(!role.equals("ADMIN")){
            logger.info("Unauthorized access: User is not an admin");
            return ResponseEntity.status(403).body("Access Denied: Only admins can delete payments");
        }
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

    @GetMapping("fetch/allpayments/{userId}")
    public ResponseEntity<?> fetchPayment(@RequestHeader("Authorization") String token, @PathVariable("userId") String userId) {
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

        logger.info("User Fetched Payments Successfully");
        return ResponseEntity.ok("Payments Fetched Successfully" + paymentRepo.findAll());

    }


}