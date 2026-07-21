package com.crm.infrastructure.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    public static final String TOPIC_LEAD_SCORED = "lead.scored";
    public static final String TOPIC_LEAD_ASSIGNED = "lead.assigned";
    public static final String TOPIC_DEAL_WON = "opportunity.deal-won";
    public static final String TOPIC_DEAL_LOST = "opportunity.deal-lost";

    @Bean
    public NewTopic leadScoredTopic() {
        return TopicBuilder.name(TOPIC_LEAD_SCORED)
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic leadAssignedTopic() {
        return TopicBuilder.name(TOPIC_LEAD_ASSIGNED)
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic dealWonTopic() {
        return TopicBuilder.name(TOPIC_DEAL_WON)
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic dealLostTopic() {
        return TopicBuilder.name(TOPIC_DEAL_LOST)
                .partitions(1)
                .replicas(1)
                .build();
    }
}
