package com.example.instructions.service;

import com.example.instructions.cache.CanonicalTradeCache;
import com.example.instructions.model.CanonicalTrade;
import com.example.instructions.util.TradeTransformer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class KafkaListenerService {
    private final KafkaPublisher publisher;
    private final TradeService tradeService;
    private final CanonicalTradeCache cache;

    public KafkaListenerService(KafkaPublisher publisher, TradeService tradeService,
                                CanonicalTradeCache cache) {
        this.publisher = publisher;
        this.tradeService = tradeService;
        this.cache = cache;
    }

    @KafkaListener(topics = "${spring.kafka.inbound-topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(byte[] message) {
        try {
            ObjectMapper mapper = new ObjectMapper();

            CanonicalTrade trade = mapper.readValue(message, CanonicalTrade.class);
            tradeService.optionalValidateTrade(trade)
                    .ifPresent(t -> {
                        cache.put(t.tradeId(), t);
                        publisher.publish(TradeTransformer.createPlatformTrade(t));
                    });
        } catch (JsonProcessingException e) {
            System.err.println("Skipping malformed JSON message " + e.getMessage());
        } catch(IOException e) {
            System.err.println("Received IO exception " + e.getMessage());
        }
    }

}