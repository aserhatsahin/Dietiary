package com.dietary.tracking.repository;

import com.dietary.tracking.domain.WaterTracking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface WaterTrackingRepository extends JpaRepository<WaterTracking, UUID> {

    @Query("SELECT wt FROM WaterTracking wt WHERE wt.client.id = :clientId AND wt.trackingDate = :date ORDER BY wt.loggedAt DESC")
    List<WaterTracking> findByClientIdAndDate(UUID clientId, LocalDate date);

    @Query("SELECT COALESCE(SUM(wt.amountMl), 0) FROM WaterTracking wt WHERE wt.client.id = :clientId AND wt.trackingDate = :date")
    int sumByClientIdAndDate(UUID clientId, LocalDate date);

    @Query("SELECT COALESCE(SUM(wt.amountMl), 0) FROM WaterTracking wt WHERE wt.client.id = :clientId AND wt.trackingDate BETWEEN :fromDate AND :toDate")
    int sumByClientIdAndDateRange(UUID clientId, LocalDate fromDate, LocalDate toDate);
}
