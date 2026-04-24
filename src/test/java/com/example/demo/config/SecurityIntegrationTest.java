package com.example.demo.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureWebMvc
class SecurityIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    @Test
    void testPublicResourcesAccessible() throws Exception {
        MockMvc mockMvc = MockMvcBuilders
            .webAppContextSetup(context)
            .apply(springSecurity())
            .build();

        // Test that Vaadin public resources are accessible without authentication
        mockMvc.perform(get("/VAADIN/resources/logo.png"))
            .andExpect(status().isNotFound()); // 404 because the resource doesn't exist, but not 401/403

        mockMvc.perform(get("/frontend/generated/vaadin.ts"))
            .andExpect(status().isNotFound()); // 404 because the resource doesn't exist, but not 401/403
    }

    @Test
    void testApiEndpointsRequireAuthentication() throws Exception {
        MockMvc mockMvc = MockMvcBuilders
            .webAppContextSetup(context)
            .apply(springSecurity())
            .build();

        // Test that API endpoints require authentication
        mockMvc.perform(post("/api/device-location/update")
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf()))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void testLoginPageAccessible() throws Exception {
        MockMvc mockMvc = MockMvcBuilders
            .webAppContextSetup(context)
            .apply(springSecurity())
            .build();

        // Test that login page is accessible
        mockMvc.perform(get("/login"))
            .andExpect(status().isOk());
    }
}