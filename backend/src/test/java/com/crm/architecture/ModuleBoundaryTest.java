package com.crm.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.RestController;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class ModuleBoundaryTest {

    private final JavaClasses importedClasses = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.crm");

    @Test
    @DisplayName("Domains outside identity must not import identity entities directly")
    void nonIdentityDomainsMustNotDependOnIdentityEntities() {
        ArchRule rule = noClasses()
                .that().resideInAnyPackage(
                        "com.crm.lead..",
                        "com.crm.customer..",
                        "com.crm.opportunity..",
                        "com.crm.task..",
                        "com.crm.workflow..",
                        "com.crm.notification..",
                        "com.crm.analytics..",
                        "com.crm.audit.."
                )
                .should().dependOnClassesThat().resideInAPackage("com.crm.identity.entity..");

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("Domains outside identity must not import identity repositories directly")
    void nonIdentityDomainsMustNotDependOnIdentityRepositories() {
        ArchRule rule = noClasses()
                .that().resideInAnyPackage(
                        "com.crm.lead..",
                        "com.crm.customer..",
                        "com.crm.opportunity..",
                        "com.crm.task..",
                        "com.crm.workflow..",
                        "com.crm.notification..",
                        "com.crm.analytics..",
                        "com.crm.audit.."
                )
                .should().dependOnClassesThat().resideInAPackage("com.crm.identity.repository..");

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("Customer domain must not import lead or opportunity entities directly")
    void customerDomainMustNotDependOnOtherDomainEntities() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("com.crm.customer..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "com.crm.lead.entity..",
                        "com.crm.lead.repository..",
                        "com.crm.opportunity.entity..",
                        "com.crm.opportunity.repository.."
                );

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("Task domain must not import lead or opportunity entities or repositories directly")
    void taskDomainMustNotDependOnOtherDomainEntities() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("com.crm.task..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "com.crm.lead.entity..",
                        "com.crm.lead.repository..",
                        "com.crm.opportunity.entity..",
                        "com.crm.opportunity.repository.."
                );

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("Domains outside organization must not import organization entities directly")
    void nonOrganizationDomainsMustNotDependOnOrganizationEntities() {
        ArchRule rule = noClasses()
                .that().resideInAnyPackage(
                        "com.crm.identity..",
                        "com.crm.lead..",
                        "com.crm.customer..",
                        "com.crm.opportunity..",
                        "com.crm.task..",
                        "com.crm.workflow..",
                        "com.crm.notification..",
                        "com.crm.analytics..",
                        "com.crm.audit.."
                )
                .should().dependOnClassesThat().resideInAPackage("com.crm.organization.entity..");

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("Domains outside organization must not import organization repositories directly")
    void nonOrganizationDomainsMustNotDependOnOrganizationRepositories() {
        ArchRule rule = noClasses()
                .that().resideInAnyPackage(
                        "com.crm.identity..",
                        "com.crm.lead..",
                        "com.crm.customer..",
                        "com.crm.opportunity..",
                        "com.crm.task..",
                        "com.crm.workflow..",
                        "com.crm.notification..",
                        "com.crm.analytics..",
                        "com.crm.audit.."
                )
                .should().dependOnClassesThat().resideInAPackage("com.crm.organization.repository..");

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("Domains outside workflow must not import workflow entities directly")
    void nonWorkflowDomainsMustNotDependOnWorkflowEntities() {
        ArchRule rule = noClasses()
                .that().resideInAnyPackage(
                        "com.crm.identity..",
                        "com.crm.lead..",
                        "com.crm.customer..",
                        "com.crm.opportunity..",
                        "com.crm.task..",
                        "com.crm.notification..",
                        "com.crm.analytics..",
                        "com.crm.audit..",
                        "com.crm.organization.."
                )
                .should().dependOnClassesThat().resideInAPackage("com.crm.workflow.entity..");

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("Domains outside workflow must not import workflow repositories directly")
    void nonWorkflowDomainsMustNotDependOnWorkflowRepositories() {
        ArchRule rule = noClasses()
                .that().resideInAnyPackage(
                        "com.crm.identity..",
                        "com.crm.lead..",
                        "com.crm.customer..",
                        "com.crm.opportunity..",
                        "com.crm.task..",
                        "com.crm.notification..",
                        "com.crm.analytics..",
                        "com.crm.audit..",
                        "com.crm.organization.."
                )
                .should().dependOnClassesThat().resideInAPackage("com.crm.workflow.repository..");

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("Domains outside analytics must not import analytics classes directly")
    void nonAnalyticsDomainsMustNotDependOnAnalyticsClasses() {
        ArchRule rule = noClasses()
                .that().resideInAnyPackage(
                        "com.crm.identity..",
                        "com.crm.lead..",
                        "com.crm.customer..",
                        "com.crm.opportunity..",
                        "com.crm.task..",
                        "com.crm.notification..",
                        "com.crm.workflow..",
                        "com.crm.audit..",
                        "com.crm.organization.."
                )
                .should().dependOnClassesThat().resideInAPackage("com.crm.analytics.service..")
                .orShould().dependOnClassesThat().resideInAPackage("com.crm.analytics.controller..");

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("Controllers must reside in controller packages")
    void controllersMustResideInControllerPackages() {
        ArchRule rule = classes()
                .that().areAnnotatedWith(RestController.class)
                .should().resideInAPackage("..controller..");

        rule.check(importedClasses);
    }
}
