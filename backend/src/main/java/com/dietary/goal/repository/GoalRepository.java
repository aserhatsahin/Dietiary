package com.dietary.goal.repository;

import com.dietary.goal.domain.Goal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface GoalRepository extends JpaRepository<Goal, UUID> {

    @Query("SELECT g FROM Goal g WHERE g.client.id = :clientId AND g.isActive = true")
    Optional<Goal> findActiveByClientId(UUID clientId);

    @Query("SELECT g FROM Goal g WHERE g.client.id = :clientId AND g.client.dietitian.id = :dietitianId AND g.isActive = true")
    Optional<Goal> findActiveByClientIdAndDietitianId(UUID clientId, UUID dietitianId);

    @Modifying
    @Query("UPDATE Goal g SET g.isActive = false WHERE g.client.id = :clientId AND g.isActive = true")
    void deactivateAllByClientId(UUID clientId);
}
