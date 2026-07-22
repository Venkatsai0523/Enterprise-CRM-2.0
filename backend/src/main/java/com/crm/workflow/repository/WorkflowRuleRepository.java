package com.crm.workflow.repository;

import com.crm.workflow.entity.WorkflowRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface WorkflowRuleRepository extends JpaRepository<WorkflowRule, UUID> {
    List<WorkflowRule> findByTriggerEventAndActiveTrue(String triggerEvent);
}
