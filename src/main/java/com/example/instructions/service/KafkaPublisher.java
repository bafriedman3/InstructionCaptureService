package com.example.instructions.service;

import com.example.instructions.config.KafkaProperties;
import com.example.instructions.model.PlatformTrade;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaPublisher {

    private final KafkaTemplate<String, PlatformTrade> kafkaTemplate;
    private final KafkaProperties kafkaProperties;

    public KafkaPublisher(KafkaTemplate<String, PlatformTrade> kafkaTemplate,
                          KafkaProperties kafkaProperties) {
        this.kafkaTemplate = kafkaTemplate;
        this.kafkaProperties = kafkaProperties;
    }

    public void publish(PlatformTrade trade) {
        kafkaTemplate.send(kafkaProperties.getOutboundTopic(), trade);
    }
}

