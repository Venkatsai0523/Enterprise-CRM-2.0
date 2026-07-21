package com.crm.infrastructure.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("null")
public class KafkaEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publish(String topic, String key, Object eventPayload) {
        log.info("Publishing event to Kafka topic '{}' with key '{}': {}", topic, key, eventPayload);
        kafkaTemplate.send(topic, key, eventPayload)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish event to topic '{}' with key '{}'", topic, key, ex);
                    } else {
                        log.debug("Successfully published event to topic '{}' at offset {}", topic, result.getRecordMetadata().offset());
                    }
                });
    }
}
