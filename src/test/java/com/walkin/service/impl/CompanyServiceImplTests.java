package com.walkin.service.impl;

import com.walkin.entity.Company;
import com.walkin.exception.ResourceNotFoundException;
import com.walkin.repository.CompanyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompanyServiceImplTests {
    @Mock CompanyRepository repository;
    @InjectMocks CompanyServiceImpl service;

    @Test void createDelegatesToRepository() {
        Company value = company("Acme"); when(repository.save(value)).thenReturn(value);
        assertSame(value, service.createCompany(value)); verify(repository).save(value);
    }
    @Test void findAllReturnsRepositoryValues() {
        List<Company> values = List.of(company("Acme")); when(repository.findAll()).thenReturn(values);
        assertSame(values, service.getAllCompanies());
    }
    @Test void missingCompanyThrowsUsefulException() {
        when(repository.findById(7)).thenReturn(Optional.empty());
        var error = assertThrows(ResourceNotFoundException.class, () -> service.getCompanyById(7));
        assertEquals("Company not found with ID: 7", error.getMessage());
    }
    @Test void updateMutatesPersistedEntity() {
        Company existing = company("Old"), update = company("New");
        when(repository.findById(1)).thenReturn(Optional.of(existing)); when(repository.save(existing)).thenReturn(existing);
        assertSame(existing, service.updateCompany(1, update)); assertEquals("New", existing.getCompanyName());
        assertEquals(update.getEmail(), existing.getEmail()); verify(repository).save(existing);
    }
    @Test void deleteLoadsBeforeDeleting() {
        Company value = company("Acme"); when(repository.findById(1)).thenReturn(Optional.of(value));
        service.deleteCompany(1); verify(repository).delete(value);
    }
    private Company company(String name) {
        Company c = new Company(); c.setCompanyName(name); c.setEmail(name.toLowerCase()+"@example.com");
        c.setContactNumber("9876543210"); c.setJobDescription("Java engineer"); return c;
    }
}
