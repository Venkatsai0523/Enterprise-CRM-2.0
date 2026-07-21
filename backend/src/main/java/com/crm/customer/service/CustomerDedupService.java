package com.crm.customer.service;

import com.crm.customer.api.dto.CustomerAccountStatus;
import com.crm.customer.entity.CustomerAccount;
import com.crm.customer.repository.CustomerAccountRepository;
import com.crm.lead.api.dto.LeadResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("null")
public class CustomerDedupService {

    private final CustomerAccountRepository customerAccountRepository;

    @Transactional
    public CustomerAccount findOrCreateCustomerAccount(LeadResponseDto lead) {
        String normalizedDomain = extractDomain(lead.getEmail());

        Optional<CustomerAccount> existingOpt = customerAccountRepository.findByDomainName(normalizedDomain);
        if (existingOpt.isPresent()) {
            log.info("Dedup Match Found! Linking opportunity to existing Customer Account ID: '{}' (Domain: '{}')",
                    existingOpt.get().getId(), normalizedDomain);
            return existingOpt.get();
        }

        // No match found -> instantiate new CustomerAccount
        CustomerAccount newAccount = CustomerAccount.builder()
                .accountName(lead.getCompanyName())
                .domainName(normalizedDomain)
                .primaryEmail(lead.getEmail())
                .phone(lead.getPhone())
                .status(CustomerAccountStatus.ACTIVE)
                .build();

        CustomerAccount savedAccount = customerAccountRepository.save(newAccount);
        log.info("No dedup match found for domain '{}'. Created new Customer Account ID: '{}'",
                normalizedDomain, savedAccount.getId());

        return savedAccount;
    }

    public String extractDomain(String email) {
        if (email == null || !email.contains("@")) {
            return "unknown.com";
        }
        return email.substring(email.indexOf("@") + 1).trim().toLowerCase();
    }
}
