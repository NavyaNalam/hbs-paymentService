package com.navya.hotelpaymentservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

        Thread.sleep(10000); // Simulate processing time
        boolean success = Math.random() > 0.3; // Simulate success or failure
        if(success)
        {
            logger.info("Payment processed successfully for booking ID: {}", bookingEvent.getBookingId());
            paymentEvent.setStatus("SUCCESS");
            Payment payment = new Payment();
            payment.setDateofPayment(LocalDate.now());
            payment.setBookingId(bookingEvent.getBookingId());
            paymentRepository.save(payment);
        } else {
            logger.error("Payment processing failed for booking ID: {}", bookingEvent.getBookingId());
            // Handle failure case, e.g., send a notification or retry
            paymentEvent.setStatus("FAILURE");
        }
        // Publish the payment event
        paymentEventProducer.publishEvent(paymentEvent);
    }
}