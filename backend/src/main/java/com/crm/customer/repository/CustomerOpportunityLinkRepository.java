package com.crm.customer.repository;

import com.crm.customer.entity.CustomerOpportunityLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerOpportunityLinkRepository extends JpaRepository<CustomerOpportunityLink, UUID> {

    List<CustomerOpportunityLink> findByCustomerAccountId(UUID customerAccountId);

    Optional<CustomerOpportunityLink> findByOpportunityId(UUID opportunityId);

    boolean existsByOpportunityId(UUID opportunityId);
}
