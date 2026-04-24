package com.example.demo.service;

import com.example.demo.model.Device;
import com.example.demo.model.User;
import com.example.demo.repository.DeviceRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.LocationHistoryRepository;
import com.example.demo.repository.DeviceEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class DeviceServiceTest {

    @Mock
    private DeviceRepository deviceRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private LocationHistoryRepository locationHistoryRepository;

    @Mock
    private DeviceEventRepository deviceEventRepository;

    @Mock
    private DeviceShareService deviceShareService;

    @Mock
    private GeofenceService geofenceService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private PerfectAuthService authService;

    @InjectMocks
    private DeviceService deviceService;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
    }

    @Test
    void testRegisterDevice() {
        // Given
        User user = new User();
        user.setEmail("test@example.com");
        
        when(deviceRepository.save(any(Device.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Device device = deviceService.registerDevice(user, "Test Device", "Test Manufacturer", "Test Model", "Test OS");

        // Then
        assertThat(device).isNotNull();
        assertThat(device.getDeviceId()).startsWith("LT-");
        assertThat(device.getDeviceName()).isEqualTo("Test Device");
        assertThat(device.getManufacturer()).isEqualTo("Test Manufacturer");
        assertThat(device.getModel()).isEqualTo("Test Model");
        assertThat(device.getOsName()).isEqualTo("Test OS");
        
        verify(deviceRepository).save(any(Device.class));
        verify(deviceEventRepository).save(any());
    }

    @Test
    void testGetDeviceById() {
        // Given
        String deviceId = "LT-12345678";
        Device device = new Device();
        device.setDeviceId(deviceId);
        
        when(deviceRepository.findByDeviceId(deviceId)).thenReturn(Optional.of(device));

        // When
        Optional<Device> result = deviceService.getDevice(deviceId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getDeviceId()).isEqualTo(deviceId);
        verify(deviceRepository).findByDeviceId(deviceId);
    }
}