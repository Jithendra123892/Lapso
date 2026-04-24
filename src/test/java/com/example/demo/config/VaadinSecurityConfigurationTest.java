package com.example.demo.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
class VaadinSecurityConfigurationTest {

    @MockBean
    private VaadinSecurityConfiguration securityConfiguration;

    @Test
    void testSecurityFilterChainConfiguration() throws Exception {
        // Given
        HttpSecurity httpSecurity = mock(HttpSecurity.class);
        when(httpSecurity.csrf(any())).thenReturn(httpSecurity);
        when(httpSecurity.headers(any())).thenReturn(httpSecurity);
        when(httpSecurity.authorizeHttpRequests(any())).thenReturn(httpSecurity);
        when(httpSecurity.formLogin(any())).thenReturn(httpSecurity);
        when(httpSecurity.logout(any())).thenReturn(httpSecurity);

        // When
        SecurityFilterChain filterChain = securityConfiguration.filterChain(httpSecurity);

        // Then
        assertThat(filterChain).isNotNull();
        verify(httpSecurity).csrf(any());
        verify(httpSecurity).headers(any());
        verify(httpSecurity).authorizeHttpRequests(any());
        verify(httpSecurity).formLogin(any());
        verify(httpSecurity).logout(any());
    }
}