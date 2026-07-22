package com.crm.workflow.engine;

import com.crm.notification.api.NotificationApi;
import com.crm.task.api.TaskApi;
import com.crm.task.api.dto.TaskCreateDto;
import com.crm.task.api.dto.TaskPriority;
import com.crm.task.api.dto.TaskType;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class WorkflowEngine {

    private final ObjectMapper objectMapper;
    private final TaskApi taskApi;
    private final NotificationApi notificationApi;

    public boolean evaluateConditions(String conditionsJson, Map<String, Object> context) {
        try {
            List<Condition> conditions = objectMapper.readValue(conditionsJson, new TypeReference<List<Condition>>() {});
            for (Condition cond : conditions) {
                if (!evaluateSingleCondition(cond, context)) {
                    return false;
                }
            }
            return true;
        } catch (Exception ex) {
            log.error("Failed to parse or evaluate conditions JSON: {}", conditionsJson, ex);
            return false;
        }
    }

    private boolean evaluateSingleCondition(Condition cond, Map<String, Object> context) {
        Object valObj = context.get(cond.getField());
        if (valObj == null) return false;

        String actual = valObj.toString();
        String expected = cond.getValue();

        switch (cond.getOperator()) {
            case "EQUALS":
                return actual.equalsIgnoreCase(expected);
            case "CONTAINS":
                return actual.toLowerCase().contains(expected.toLowerCase());
            case "GREATER_THAN":
                return new BigDecimal(actual).compareTo(new BigDecimal(expected)) > 0;
            case "LESS_THAN":
                return new BigDecimal(actual).compareTo(new BigDecimal(expected)) < 0;
            default:
                return false;
        }
    }

    public void executeActions(String actionsJson, Map<String, Object> context, UUID orgId) {
        try {
            List<Action> actions = objectMapper.readValue(actionsJson, new TypeReference<List<Action>>() {});
            for (Action action : actions) {
                executeSingleAction(action, context, orgId);
            }
        } catch (Exception ex) {
            log.error("Failed to execute workflow actions: {}", actionsJson, ex);
        }
    }

    private void executeSingleAction(Action action, Map<String, Object> context, UUID orgId) {
        switch (action.getType()) {
            case "SEND_NOTIFICATION":
                String recipientIdStr = action.getParameters().get("recipientId");
                UUID recipientId = recipientIdStr != null ? UUID.fromString(recipientIdStr) : (UUID) context.get("assignedRepId");
                
                String messageTemplate = action.getParameters().get("messageTemplate");
                String message = interpolate(messageTemplate, context);

                if (recipientId != null) {
                    notificationApi.sendNotification(recipientId, "WORKFLOW_ALERT", message);
                }
                break;

            case "CREATE_TASK":
                String titleTemplate = action.getParameters().get("title");
                String title = interpolate(titleTemplate, context);
                String priorityStr = action.getParameters().getOrDefault("priority", "MEDIUM");
                String daysToDueStr = action.getParameters().getOrDefault("daysToDue", "3");

                UUID assignedTo = (UUID) context.get("assignedRepId");
                UUID relatedToId = (UUID) context.get("entityId");
                String relatedToType = (String) context.get("entityType");

                if (assignedTo == null) {
                    assignedTo = UUID.fromString("e8888888-8888-8888-8888-888888888888"); // default fallback
                }

                TaskCreateDto taskDto = TaskCreateDto.builder()
                        .title(title)
                        .description("Automated workflow follow-up task.")
                        .dueDate(Instant.now().plus(Integer.parseInt(daysToDueStr), ChronoUnit.DAYS))
                        .priority(TaskPriority.valueOf(priorityStr.toUpperCase()))
                        .type(TaskType.TASK)
                        .assignedTo(assignedTo)
                        .relatedToType(relatedToType)
                        .relatedToId(relatedToId)
                        .build();

                taskApi.createTask(taskDto);
                log.info("Created workflow automated task: {}", title);
                break;

            default:
                log.warn("Unknown workflow action type: {}", action.getType());
        }
    }

    private String interpolate(String template, Map<String, Object> context) {
        if (template == null) return "";
        String result = template;
        for (Map.Entry<String, Object> entry : context.entrySet()) {
            if (entry.getValue() != null) {
                result = result.replace("{" + entry.getKey() + "}", entry.getValue().toString());
            }
        }
        return result;
    }

    @lombok.Data
    public static class Condition {
        private String field;
        private String operator;
        private String value;
    }

    @lombok.Data
    public static class Action {
        private String type;
        private Map<String, String> parameters;
    }
}
