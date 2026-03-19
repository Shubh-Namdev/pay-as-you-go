package com.sunking.payg.kafka.producer;

import com.sunking.payg.kafka.event.PaymentEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentProducer {

    private final KafkaTemplate<String, PaymentEvent> kafkaTemplate;

    private static final String TOPIC = "payment-events";

    public void publish(PaymentEvent event) {
        kafkaTemplate.send(TOPIC, event);
    }
}