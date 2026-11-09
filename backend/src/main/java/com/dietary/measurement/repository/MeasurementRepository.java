package com.dietary.measurement.repository;

import com.dietary.measurement.domain.Measurement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MeasurementRepository extends JpaRepository<Measurement, UUID> {

    // Newest first. Same-day measurements are ordered by when they were entered.

    @Query("SELECT m FROM Measurement m WHERE m.client.id = :clientId ORDER BY m.measurementDate DESC, m.createdAt DESC")
    List<Measurement> findAllByClientIdOrderByDateDesc(UUID clientId);

    @Query("SELECT m FROM Measurement m WHERE m.client.id = :clientId ORDER BY m.measurementDate DESC, m.createdAt DESC LIMIT 1")
    Optional<Measurement> findLatestByClientId(UUID clientId);

    /** The measurement taken just before the given one (by date, then entry time). */
    @Query("SELECT m FROM Measurement m WHERE m.client.id = :clientId AND m.id <> :id AND " +
            "(m.measurementDate < :date OR (m.measurementDate = :date AND m.createdAt < :createdAt)) " +
            "ORDER BY m.measurementDate DESC, m.createdAt DESC LIMIT 1")
    Optional<Measurement> findPrevious(UUID clientId, UUID id, LocalDate date, Instant createdAt);

    @Query("SELECT m FROM Measurement m WHERE m.client.id = :clientId AND m.client.dietitian.id = :dietitianId ORDER BY m.measurementDate DESC, m.createdAt DESC")
    List<Measurement> findAllByClientIdAndDietitianId(UUID clientId, UUID dietitianId);

    @Query("SELECT m FROM Measurement m WHERE m.id = :id AND m.client.id = :clientId AND m.client.dietitian.id = :dietitianId")
    Optional<Measurement> findByIdAndClientIdAndDietitianId(UUID id, UUID clientId, UUID dietitianId);

    boolean existsByClientIdAndClientDietitianId(UUID clientId, UUID dietitianId);
}
