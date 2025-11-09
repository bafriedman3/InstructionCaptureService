package com.example.instructions.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "spring.kafka")
public class KafkaProperties {
    private String inboundTopic;
    private String outboundTopic;
    private String bootStrapServers;

    public String getBootStrapServers() {
        return bootStrapServers;
    }

    public void setBootStrapServers(String bootStrapServers) {
        this.bootStrapServers = bootStrapServers;
    }

    public String getInboundTopic() {
        return inboundTopic;
    }

    public void setInboundTopic(String inboundTopic) {
        this.inboundTopic = inboundTopic;
    }

    public String getOutboundTopic() {
        return outboundTopic;
    }

    public void setOutboundTopic(String outboundTopic) {
        this.outboundTopic = outboundTopic;
    }
}
