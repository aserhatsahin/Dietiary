package com.dietary.mealplan.repository;

import com.dietary.mealplan.domain.MealPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MealPlanRepository extends JpaRepository<MealPlan, UUID> {

    @Query("SELECT mp FROM MealPlan mp WHERE mp.dietitian.id = :dietitianId AND mp.client IS NULL ORDER BY mp.name")
    List<MealPlan> findAllTemplatesByDietitianId(UUID dietitianId);

    @Query("SELECT mp FROM MealPlan mp WHERE mp.id = :id AND mp.dietitian.id = :dietitianId")
    Optional<MealPlan> findByIdAndDietitianId(UUID id, UUID dietitianId);

    @Query("SELECT mp FROM MealPlan mp WHERE mp.client.id = :clientId AND mp.isActive = true")
    Optional<MealPlan> findActiveByClientId(UUID clientId);

    @Query("SELECT mp FROM MealPlan mp WHERE mp.client.id = :clientId AND mp.client.dietitian.id = :dietitianId AND mp.isActive = true")
    Optional<MealPlan> findActiveByClientIdAndDietitianId(UUID clientId, UUID dietitianId);

    @Query("SELECT mp FROM MealPlan mp WHERE mp.client.id = :clientId AND mp.client.dietitian.id = :dietitianId ORDER BY mp.createdAt DESC")
    List<MealPlan> findAllByClientIdAndDietitianId(UUID clientId, UUID dietitianId);

    @Modifying
    @Query("UPDATE MealPlan mp SET mp.isActive = false WHERE mp.client.id = :clientId AND mp.isActive = true")
    void deactivateAllByClientId(UUID clientId);
}
