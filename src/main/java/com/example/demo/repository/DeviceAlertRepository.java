package com.example.demo.repository;

import com.example.demo.model.DeviceAlert;
import com.example.demo.model.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DeviceAlertRepository extends JpaRepository<DeviceAlert, Long> {
    
    List<DeviceAlert> findByLaptopOrderByCreatedAtDesc(Device laptop);
    
    @Query("SELECT da FROM DeviceAlert da WHERE da.laptop.user.email = :email ORDER BY da.createdAt DESC")
    List<DeviceAlert> findByUserEmailOrderByCreatedAtDesc(@Param("email") String email);
    
    @Query("SELECT da FROM DeviceAlert da WHERE da.laptop.user.email = :email AND da.isRead = false ORDER BY da.createdAt DESC")
    List<DeviceAlert> findUnreadByUserEmail(@Param("email") String email);
    
    @Query("SELECT COUNT(da) FROM DeviceAlert da WHERE da.laptop.user.email = :email AND da.isRead = false")
    Long countUnreadByUserEmail(@Param("email") String email);
    
    List<DeviceAlert> findByAlertTypeAndCreatedAtAfter(DeviceAlert.AlertType alertType, LocalDateTime after);
    
    List<DeviceAlert> findBySeverityOrderByCreatedAtDesc(DeviceAlert.Severity severity);
    
    @Query("SELECT da FROM DeviceAlert da WHERE da.laptop = :laptop AND da.alertType = :alertType ORDER BY da.createdAt DESC LIMIT 1")
    DeviceAlert findLatestByLaptopAndAlertType(@Param("laptop") Device laptop, @Param("alertType") DeviceAlert.AlertType alertType);
    
    void deleteByLaptopAndCreatedAtBefore(Device laptop, LocalDateTime before);
}
