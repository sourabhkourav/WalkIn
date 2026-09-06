package com.walkin.controller;

import com.walkin.repository.ApplicationUserRepository;
import com.walkin.repository.CompanyRepository;
import com.walkin.entity.ApplicationUser;
import com.walkin.entity.Company;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest @ActiveProfiles("test")
class AuthControllerTests {
    @Autowired WebApplicationContext context;
    @Autowired ApplicationUserRepository users;
    @Autowired PasswordEncoder encoder;
    @Autowired CompanyRepository companies;
    @Autowired JwtDecoder jwtDecoder;
    MockMvc mockMvc;
    @BeforeEach void setUp() { mockMvc=MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build(); }

    @Test void bootstrapAdminIsStoredWithBcryptNotPlainText() {
        var admin=users.findByUsernameIgnoreCase("test-admin").orElseThrow();
        assertNotEquals("test-password", admin.getPasswordHash()); assertTrue(encoder.matches("test-password", admin.getPasswordHash()));
    }
    @Test void validLoginReturnsBearerToken() throws Exception {
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"test-admin\",\"password\":\"test-password\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.accessToken").isNotEmpty()).andExpect(jsonPath("$.expiresAt").exists());
    }
    @Test void companyLoginJwtContainsCompanyIdentity() throws Exception {
        Company company = new Company();
        company.setCompanyName("JWT Tenant");
        company.setEmail("jwt-tenant@example.com");
        company.setContactNumber("+919999999991");
        company.setJobDescription("Engineering hiring");
        company = companies.save(company);

        ApplicationUser companyAdmin = new ApplicationUser();
        companyAdmin.setUsername("jwt-company-admin");
        companyAdmin.setPasswordHash(encoder.encode("company-password"));
        companyAdmin.setRole(ApplicationUser.Role.COMPANY_ADMIN);
        companyAdmin.setCompany(company);
        users.save(companyAdmin);

        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"jwt-company-admin\",\"password\":\"company-password\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String accessToken = JsonPath.read(response, "$.accessToken");

        var jwt = jwtDecoder.decode(accessToken);
        assertEquals(company.getCompanyId(), ((Number) jwt.getClaim("companyId")).intValue());
        assertEquals("ROLE_COMPANY_ADMIN", jwt.getClaimAsString("roles"));
    }
    @Test void invalidLoginReturnsUnauthorizedWithoutLeakingDetails() throws Exception {
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"test-admin\",\"password\":\"wrong-password\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid username or password"));
    }
    @Test void recruiterCannotAccessLegacyUnscopedResources() throws Exception {
        var recruiter=jwt().jwt(j -> j.subject("recruiter").claim("roles", "ROLE_RECRUITER"))
                .authorities(() -> "ROLE_RECRUITER");
        mockMvc.perform(get("/api/students").with(recruiter)).andExpect(status().isForbidden());
        mockMvc.perform(delete("/api/students/1").with(recruiter)).andExpect(status().isForbidden());
    }
}
