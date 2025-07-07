package com.navya.hotelpaymentservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class PaymentEventProducer {

    private static final String TOPIC = "payment-events";

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    public void publishEvent(PaymentEvent event) {

        ObjectMapper objectMapper = new ObjectMapper();
        try{
            String eventJson = objectMapper.writeValueAsString(event);
            this.kafkaTemplate.send(TOPIC, eventJson);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize event: " + event, e);
        }
    }
}
