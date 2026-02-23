package com.dietary.tracking.repository;

import com.dietary.tracking.domain.DailyTracking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DailyTrackingRepository extends JpaRepository<DailyTracking, UUID> {

    @Query("SELECT dt FROM DailyTracking dt WHERE dt.client.id = :clientId AND dt.trackingDate = :date")
    List<DailyTracking> findByClientIdAndDate(UUID clientId, LocalDate date);

    @Query("SELECT dt FROM DailyTracking dt WHERE dt.client.id = :clientId AND dt.trackingDate = :date AND dt.meal.id = :mealId")
    Optional<DailyTracking> findByClientIdAndDateAndMealId(UUID clientId, LocalDate date, UUID mealId);

    @Query("SELECT dt FROM DailyTracking dt WHERE dt.client.id = :clientId AND dt.trackingDate BETWEEN :fromDate AND :toDate")
    List<DailyTracking> findByClientIdAndDateRange(UUID clientId, LocalDate fromDate, LocalDate toDate);

    @Query("SELECT COUNT(dt) FROM DailyTracking dt WHERE dt.client.id = :clientId AND dt.isCompleted = true AND dt.trackingDate BETWEEN :fromDate AND :toDate")
    long countCompletedByClientIdAndDateRange(UUID clientId, LocalDate fromDate, LocalDate toDate);
}
