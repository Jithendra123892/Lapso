package com.example.demo.repository;

import com.example.demo.model.Geofence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface GeofenceRepository extends JpaRepository<Geofence, Long> {
    
    List<Geofence> findByUserIdAndIsActiveTrue(String userId);
    
    List<Geofence> findByUserId(String userId);
    
    @Query("SELECT g FROM Geofence g WHERE g.userId = :userId AND g.isActive = true " +
           "AND g.fenceType = :fenceType")
    List<Geofence> findActiveGeofencesByUserAndType(@Param("userId") String userId, 
                                                    @Param("fenceType") Geofence.GeofenceType fenceType);
    
    @Query("SELECT g FROM Geofence g WHERE g.userId = :userId AND g.isActive = true " +
           "AND (6371000 * acos(cos(radians(:latitude)) * cos(radians(g.centerLatitude)) * " +
           "cos(radians(g.centerLongitude) - radians(:longitude)) + " +
           "sin(radians(:latitude)) * sin(radians(g.centerLatitude)))) <= g.radiusMeters")
    List<Geofence> findGeofencesContainingPoint(@Param("userId") String userId,
                                               @Param("latitude") Double latitude,
                                               @Param("longitude") Double longitude);
    
    @Query("SELECT COUNT(g) FROM Geofence g WHERE g.userId = :userId AND g.isActive = true")
    Long countActiveGeofencesByUser(@Param("userId") String userId);
    
    List<Geofence> findByUserIdAndNameContainingIgnoreCase(String userId, String name);
}
