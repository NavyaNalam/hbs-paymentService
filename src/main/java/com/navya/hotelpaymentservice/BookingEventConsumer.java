package com.navya.hotelpaymentservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.time.LocalDate;

@Service
public class BookingEventConsumer {
    private static final Logger logger = LoggerFactory.getLogger(BookingEventConsumer.class);

    @Autowired
    private PaymentEventProducer paymentEventProducer;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;


    @KafkaListener(topics = "booking-events", groupId = "booking-payment-manager-group")
    public void consume(String message) throws InterruptedException, IOException {
        logger.info("Received Booking event: {}", message);

        ObjectMapper objectMapper = new ObjectMapper();
        BookingEvent bookingEvent;

        bookingEvent = objectMapper.readValue(message, BookingEvent.class);

        // Add your event handling logic here
        logger.info("Processing Payment for booking ID: {}", bookingEvent.getBookingId());

        PaymentEvent paymentEvent = new PaymentEvent();
        paymentEvent.setBookingId(bookingEvent.getBookingId());
        paymentEvent.setTotalFare(bookingEvent.getTotalFare());

        //Add the payment processing logic here
        // Simulate payment processing

        Thread.sleep(10000); // Simulate processing time
        //boolean success = Math.random() > 0.3; // Simulate success or failure
        boolean success = true;
        if(success)
        {
            logger.info("Payment processed successfully for booking ID: {}", bookingEvent.getBookingId());

            Payment newPayment = new Payment();
            newPayment.setDateofPayment(LocalDate.now());
            newPayment.setBookingId(bookingEvent.getBookingId());
            newPayment.setTotalPrice(bookingEvent.getTotalFare());
            paymentEvent.setStatus("SUCCESS");
            paymentRepository.save(newPayment);
            paymentEvent.setPaymentId(newPayment.getPaymentId());
            logger.info("Payment ID generated is: {}", newPayment.getPaymentId());
            //Add Caching logic here if needed
            redisTemplate.opsForValue().set(paymentEvent.getBookingId().toString(), "PAYMENT SUCCEEDED");
        } else {
            logger.error("Payment processing failed for booking ID: {}", bookingEvent.getBookingId());
            // Handle failure case, e.g., send a notification or retry
            paymentEvent.setStatus("FAILURE");
            redisTemplate.opsForValue().set(paymentEvent.getBookingId().toString(), "PAYMENT FAILURE");
        }
        // Publish the payment event
        logger.info("Proceeding to publish the payment event for booking ID: {}", bookingEvent.getBookingId());
        paymentEventProducer.publishEvent(paymentEvent);

    }
}