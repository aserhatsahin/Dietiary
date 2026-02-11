package com.dietary.measurement.repository;

import com.dietary.measurement.domain.Measurement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MeasurementRepository extends JpaRepository<Measurement, UUID> {

    @Query("SELECT m FROM Measurement m WHERE m.client.id = :clientId ORDER BY m.measurementDate DESC")
    List<Measurement> findAllByClientIdOrderByDateDesc(UUID clientId);

    @Query("SELECT m FROM Measurement m WHERE m.client.id = :clientId ORDER BY m.measurementDate DESC LIMIT 1")
    Optional<Measurement> findLatestByClientId(UUID clientId);

    @Query("SELECT m FROM Measurement m WHERE m.client.id = :clientId AND m.id != :excludeId ORDER BY m.measurementDate DESC LIMIT 1")
    Optional<Measurement> findPreviousByClientId(UUID clientId, UUID excludeId);

    @Query("SELECT m FROM Measurement m WHERE m.client.id = :clientId AND m.client.dietitian.id = :dietitianId ORDER BY m.measurementDate DESC")
    List<Measurement> findAllByClientIdAndDietitianId(UUID clientId, UUID dietitianId);

    boolean existsByClientIdAndClientDietitianId(UUID clientId, UUID dietitianId);
}
