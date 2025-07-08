package com.navya.hotelpaymentservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class PaymentEventProducer {

    private static final String TOPIC = "payment-events";
    private static final Logger logger = LoggerFactory.getLogger(PaymentEventProducer.class);

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    public void publishEvent(PaymentEvent event) {
        logger.info("Publishing Payment event: {}", event);
        ObjectMapper objectMapper = new ObjectMapper();
        try{
            String eventJson = objectMapper.writeValueAsString(event);
            logger.info("Sending the Payment event to kafka");
            this.kafkaTemplate.send(TOPIC, eventJson);
        } catch (Exception e) {
            logger.info("Failed to serialize event: {}", event, e);
            throw new RuntimeException("Failed to serialize event: " + event, e);
        }
    }
}
