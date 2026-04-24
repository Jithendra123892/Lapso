package com.example.demo.controller;

import com.example.demo.model.Device;
import com.example.demo.model.User;
import com.example.demo.service.DeviceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DeviceRegistrationController.class)
class DeviceRegistrationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DeviceService deviceService;

    @Test
    @WithMockUser
    void testRegisterDeviceEndpoint() throws Exception {
        // Given
        Device device = new Device();
        device.setDeviceId("LT-12345678");
        device.setDeviceName("Test Device");
        
        when(deviceService.registerDevice(any(User.class), anyString(), anyString(), anyString(), anyString()))
            .thenReturn(device);

        // When & Then
        mockMvc.perform(post("/api/device/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"deviceName\":\"Test Device\",\"manufacturer\":\"Test Manufacturer\",\"model\":\"Test Model\",\"osName\":\"Test OS\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deviceId").value("LT-12345678"))
                .andExpect(jsonPath("$.deviceName").value("Test Device"));
    }

    @Test
    void testRegisterDeviceWithoutAuthentication() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/device/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"deviceName\":\"Test Device\",\"manufacturer\":\"Test Manufacturer\",\"model\":\"Test Model\",\"osName\":\"Test OS\"}"))
                .andExpect(status().isUnauthorized());
    }
}