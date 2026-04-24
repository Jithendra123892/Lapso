package com.example.demo.repository;

import com.example.demo.model.SmartAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SmartAlertRepository extends JpaRepository<SmartAlert, Long> {
    
    List<SmartAlert> findByUserIdOrderByCreatedAtDesc(String userId);
    
    List<SmartAlert> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);
    
    List<SmartAlert> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(String userId);
    
    List<SmartAlert> findByUserIdAndIsAcknowledgedFalseOrderByCreatedAtDesc(String userId);
    
    List<SmartAlert> findByDeviceIdOrderByCreatedAtDesc(String deviceId);
    
    List<SmartAlert> findByUserIdAndPriorityOrderByCreatedAtDesc(String userId, SmartAlert.AlertPriority priority);
    
    List<SmartAlert> findByUserIdAndAlertTypeOrderByCreatedAtDesc(String userId, SmartAlert.AlertType alertType);
    
    @Query("SELECT a FROM SmartAlert a WHERE a.userId = :userId AND a.priority IN :priorities " +
           "ORDER BY a.createdAt DESC")
    List<SmartAlert> findByUserIdAndPrioritiesIn(@Param("userId") String userId, 
                                                 @Param("priorities") List<SmartAlert.AlertPriority> priorities);
    
    @Query("SELECT a FROM SmartAlert a WHERE a.userId = :userId AND a.createdAt >= :since " +
           "ORDER BY a.createdAt DESC")
    List<SmartAlert> findRecentAlerts(@Param("userId") String userId, 
                                     @Param("since") LocalDateTime since);
    
    @Query("SELECT COUNT(a) FROM SmartAlert a WHERE a.userId = :userId AND a.isRead = false")
    Long countUnreadAlerts(@Param("userId") String userId);
    
    @Query("SELECT COUNT(a) FROM SmartAlert a WHERE a.userId = :userId AND a.isAcknowledged = false " +
           "AND a.priority IN ('CRITICAL', 'EMERGENCY')")
    Long countCriticalUnacknowledgedAlerts(@Param("userId") String userId);
    
    @Query("SELECT a FROM SmartAlert a WHERE a.userId = :userId AND a.geofenceId = :geofenceId " +
           "ORDER BY a.createdAt DESC")
    List<SmartAlert> findByUserIdAndGeofenceId(@Param("userId") String userId, 
                                              @Param("geofenceId") Long geofenceId);
    
    @Query("SELECT COUNT(a) FROM SmartAlert a WHERE a.deviceId = :deviceId " +
           "AND a.createdAt >= :since AND a.alertType = :alertType")
    Long countAlertsForDeviceSince(@Param("deviceId") String deviceId,
                                  @Param("since") LocalDateTime since,
                                  @Param("alertType") SmartAlert.AlertType alertType);
    
    // Analytics queries
    @Query("SELECT a.alertType, COUNT(a) FROM SmartAlert a WHERE a.userId = :userId " +
           "AND a.createdAt >= :since GROUP BY a.alertType")
    List<Object[]> getAlertStatistics(@Param("userId") String userId, 
                                     @Param("since") LocalDateTime since);
    
    @Query("SELECT DATE(a.createdAt), COUNT(a) FROM SmartAlert a WHERE a.userId = :userId " +
           "AND a.createdAt >= :since GROUP BY DATE(a.createdAt) ORDER BY DATE(a.createdAt)")
    List<Object[]> getDailyAlertCounts(@Param("userId") String userId, 
                                      @Param("since") LocalDateTime since);
}
