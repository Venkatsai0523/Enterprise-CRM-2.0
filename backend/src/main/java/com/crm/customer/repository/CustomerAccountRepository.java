package com.crm.customer.repository;

import com.crm.customer.entity.CustomerAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerAccountRepository extends JpaRepository<CustomerAccount, UUID> {

    Optional<CustomerAccount> findByDomainName(String domainName);

    Optional<CustomerAccount> findByPrimaryEmail(String primaryEmail);

    boolean existsByDomainName(String domainName);
}
