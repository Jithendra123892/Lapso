package com.example.demo.repository;

import com.example.demo.model.Device;
import com.example.demo.model.LocationShareLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LocationShareLinkRepository extends JpaRepository<LocationShareLink, Long> {
    
    LocationShareLink findByToken(String token);
    
    List<LocationShareLink> findByDeviceAndIsActiveTrue(Device device);
    
    List<LocationShareLink> findByOwnerEmail(String ownerEmail);
    
    List<LocationShareLink> findByOwnerEmailAndIsActiveTrue(String ownerEmail);
    
    @Query("SELECT l FROM LocationShareLink l WHERE l.isActive = true AND " +
           "(l.expiresAt IS NULL OR l.expiresAt > :now) AND " +
           "(l.maxViews IS NULL OR l.viewCount < l.maxViews)")
    List<LocationShareLink> findAllValidLinks(@Param("now") LocalDateTime now);
    
    @Query("SELECT l FROM LocationShareLink l WHERE l.device = :device AND l.isActive = true AND " +
           "(l.expiresAt IS NULL OR l.expiresAt > :now) AND " +
           "(l.maxViews IS NULL OR l.viewCount < l.maxViews)")
    List<LocationShareLink> findValidLinksByDevice(@Param("device") Device device, 
                                                   @Param("now") LocalDateTime now);
    
    @Query("SELECT COUNT(l) FROM LocationShareLink l WHERE l.ownerEmail = :ownerEmail AND l.isActive = true")
    Long countActiveByOwner(@Param("ownerEmail") String ownerEmail);
    
    @Query("SELECT l FROM LocationShareLink l WHERE l.isActive = true AND l.expiresAt IS NOT NULL AND l.expiresAt < :now")
    List<LocationShareLink> findExpiredLinks(@Param("now") LocalDateTime now);
}
