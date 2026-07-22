package com.crm.notification;

import com.crm.infrastructure.kafka.KafkaEventPublisher;
import com.crm.infrastructure.kafka.KafkaTopicConfig;
import com.crm.lead.api.event.LeadAssignedEvent;
import com.crm.notification.repository.NotificationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class NotificationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private KafkaEventPublisher kafkaEventPublisher;

    @Autowired
    private NotificationRepository notificationRepository;

    @Test
    @WithMockUser(roles = "SALES_REP")
    @DisplayName("Kafka Event -> Notification Consumption -> HTTP Retrieval -> Mark as Read")
    void notificationLifecycleTest() throws Exception {
        UUID repId = UUID.randomUUID();
        UUID leadId = UUID.randomUUID();

        // 1. Publish LeadAssignedEvent to Kafka
        LeadAssignedEvent assignedEvent = LeadAssignedEvent.builder()
                .leadId(leadId)
                .assignedRepId(repId)
                .score(85)
                .build();

        kafkaEventPublisher.publish(KafkaTopicConfig.TOPIC_LEAD_ASSIGNED, leadId.toString(), assignedEvent);

        // 2. Await NotificationConsumer to persist notification
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            long count = notificationRepository.findByRecipientIdAndReadFalseOrderByCreatedAtDesc(repId).size();
            assertThat(count).isGreaterThanOrEqualTo(1);
        });

        // 3. Query GET /api/notifications?recipientId={repId}
        MvcResult getResult = mockMvc.perform(get("/api/notifications")
                        .param("recipientId", repId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].type").value("LEAD_ASSIGNED"))
                .andExpect(jsonPath("$.data.content[0].read").value(false))
                .andReturn();

        String contentString = getResult.getResponse().getContentAsString();
        UUID notificationId = UUID.fromString(objectMapper.readTree(contentString)
                .get("data").get("content").get(0).get("id").asText());

        // 4. Mark notification as read PATCH /api/notifications/{id}/read
        mockMvc.perform(patch("/api/notifications/" + notificationId + "/read"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.read").value(true));
    }
}
