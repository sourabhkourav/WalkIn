package com.walkin;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
class OpenApiTests {
    @Autowired WebApplicationContext context;
    MockMvc mockMvc;

    @BeforeEach void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    @Test void openApiDocumentIsPublicAndDescribesAuthenticationAndResources() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.info.title").value("WalkIn API"))
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.scheme").value("bearer"))
                .andExpect(jsonPath("$.paths['/api/students']").exists())
                .andExpect(jsonPath("$.paths['/api/companies']").exists())
                .andExpect(jsonPath("$.paths['/api/applications']").exists())
                .andExpect(jsonPath("$.paths['/api/candidate-round-schedules']").exists())
                .andExpect(jsonPath("$.paths['/api/candidate-round-schedules/due']").exists())
                .andExpect(jsonPath("$.paths['/api/candidate-round-schedules/{id}/status']").exists())
                .andExpect(jsonPath("$.paths['/api/hiring-drives']").exists())
                .andExpect(jsonPath("$.paths['/api/hiring-drives/{id}/status']").exists())
                .andExpect(jsonPath(
                        "$.paths['/api/hiring-drives/{id}/registration-form']").exists())
                .andExpect(jsonPath("$.paths['/api/hiring-drives/{driveId}/rounds']").exists())
                .andExpect(jsonPath("$.paths['/api/public/hiring-drives/{registrationToken}']").exists())
                .andExpect(jsonPath(
                        "$.paths['/api/public/hiring-drives/{registrationToken}'].get.security")
                        .isEmpty());
    }

    @Test void swaggerUiIsPublic() throws Exception {
        mockMvc.perform(get("/swagger-ui.html"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/swagger-ui/index.html"));
    }
}
