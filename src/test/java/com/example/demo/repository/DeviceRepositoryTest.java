package com.example.demo.repository;

import com.example.demo.model.Device;
import com.example.demo.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class DeviceRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private DeviceRepository deviceRepository;

    @Test
    void testFindByDeviceId() {
        // Given
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("password");
        entityManager.persistAndFlush(user);

        Device device = new Device();
        device.setDeviceId("LT-12345678");
        device.setDeviceName("Test Device");
        device.setUser(user);
        device.setLastSeen(LocalDateTime.now());
        entityManager.persistAndFlush(device);

        // When
        var result = deviceRepository.findByDeviceId("LT-12345678");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getDeviceId()).isEqualTo("LT-12345678");
        assertThat(result.get().getDeviceName()).isEqualTo("Test Device");
    }

    @Test
    void testFindByUserEmail() {
        // Given
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("password");
        entityManager.persistAndFlush(user);

        Device device1 = new Device();
        device1.setDeviceId("LT-11111111");
        device1.setDeviceName("Device 1");
        device1.setUser(user);
        entityManager.persistAndFlush(device1);

        Device device2 = new Device();
        device2.setDeviceId("LT-22222222");
        device2.setDeviceName("Device 2");
        device2.setUser(user);
        entityManager.persistAndFlush(device2);

        // When
        List<Device> devices = deviceRepository.findByUserEmail("test@example.com");

        // Then
        assertThat(devices).hasSize(2);
        assertThat(devices).extracting(Device::getDeviceId)
            .containsExactlyInAnyOrder("LT-11111111", "LT-22222222");
    }
}