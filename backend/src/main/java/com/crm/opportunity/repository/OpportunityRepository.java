package com.crm.opportunity.repository;

import com.crm.opportunity.api.dto.LostAnalysisDto;
import com.crm.opportunity.api.dto.OpportunityStage;
import com.crm.opportunity.entity.Opportunity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OpportunityRepository extends JpaRepository<Opportunity, UUID> {

    @Query("SELECT o FROM Opportunity o WHERE " +
           "(:stage IS NULL OR o.stage = :stage) AND " +
           "(:leadId IS NULL OR o.leadId = :leadId)")
    Page<Opportunity> findWithFilters(
            @Param("stage") OpportunityStage stage,
            @Param("leadId") UUID leadId,
            Pageable pageable
    );

    @Query("SELECT new com.crm.opportunity.api.dto.LostAnalysisDto(o.lostReason, COUNT(o), SUM(o.estimatedValue)) " +
           "FROM Opportunity o WHERE o.stage = com.crm.opportunity.api.dto.OpportunityStage.LOST GROUP BY o.lostReason")
    List<LostAnalysisDto> aggregateLostAnalysis();
}
