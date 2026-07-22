package com.crm.customer.service;

import com.crm.common.exception.ResourceNotFoundException;
import com.crm.customer.api.CustomerApi;
import com.crm.customer.api.dto.Customer360ResponseDto;
import com.crm.customer.api.dto.CustomerAccountResponseDto;
import com.crm.customer.entity.CustomerAccount;
import com.crm.customer.entity.CustomerOpportunityLink;
import com.crm.customer.mapper.CustomerMapper;
import com.crm.customer.repository.CustomerAccountRepository;
import com.crm.customer.repository.CustomerOpportunityLinkRepository;
import com.crm.opportunity.api.OpportunityApi;
import com.crm.opportunity.api.dto.OpportunityResponseDto;
import com.crm.task.api.TaskApi;
import com.crm.task.api.dto.TaskResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("null")
public class CustomerService implements CustomerApi {

    private final CustomerAccountRepository customerAccountRepository;
    private final CustomerOpportunityLinkRepository linkRepository;
    private final CustomerMapper customerMapper;
    private final OpportunityApi opportunityApi;
    private final TaskApi taskApi;

    @Transactional(readOnly = true)
    public Customer360ResponseDto getCustomer360(UUID customerAccountId) {
        CustomerAccount account = customerAccountRepository.findById(customerAccountId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer account not found with ID: " + customerAccountId));

        List<CustomerOpportunityLink> links = linkRepository.findByCustomerAccountId(customerAccountId);

        List<OpportunityResponseDto> linkedOpportunities = links.stream()
                .map(link -> opportunityApi.findOpportunityById(link.getOpportunityId()))
                .flatMap(Optional::stream)
                .toList();

        BigDecimal totalLifetimeValue = linkedOpportunities.stream()
                .map(OpportunityResponseDto::getEstimatedValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<TaskResponseDto> activities = linkedOpportunities.stream()
                .flatMap(opp -> taskApi.findTasksByRelatedObject("OPPORTUNITY", opp.getId()).stream())
                .toList();

        return Customer360ResponseDto.builder()
                .id(account.getId())
                .accountName(account.getAccountName())
                .domainName(account.getDomainName())
                .primaryEmail(account.getPrimaryEmail())
                .phone(account.getPhone())
                .status(account.getStatus())
                .totalLifetimeValue(totalLifetimeValue)
                .opportunityCount(linkedOpportunities.size())
                .linkedOpportunities(linkedOpportunities)
                .activities(activities)
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .build();
    }

    @Transactional(readOnly = true)
    public Page<CustomerAccountResponseDto> getCustomerAccounts(int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return customerAccountRepository.findAll(pageRequest).map(customerMapper::toDto);
    }

    // --- CustomerApi Implementation ---

    @Override
    @Transactional(readOnly = true)
    public Optional<CustomerAccountResponseDto> findCustomerById(UUID customerId) {
        return customerAccountRepository.findById(customerId).map(customerMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CustomerAccountResponseDto> findCustomerByDomain(String domainName) {
        return customerAccountRepository.findByDomainName(domainName).map(customerMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(UUID customerId) {
        return customerAccountRepository.existsById(customerId);
    }
}
