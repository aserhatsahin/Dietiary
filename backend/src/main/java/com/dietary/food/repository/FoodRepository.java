package com.dietary.food.repository;

import com.dietary.food.domain.Food;
import com.dietary.food.domain.FoodSource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FoodRepository extends JpaRepository<Food, UUID> {

    @Query("SELECT f FROM Food f WHERE f.active = true AND " +
            "(f.source = 'SYSTEM' OR f.dietitian.id = :dietitianId) AND " +
            "(:query IS NULL OR LOWER(f.name) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
            "(:category IS NULL OR LOWER(f.category) = LOWER(:category)) AND " +
            "(:source IS NULL OR f.source = :source) " +
            "ORDER BY f.name")
    List<Food> searchFoods(UUID dietitianId, String query, String category, FoodSource source);

    @Query("SELECT f FROM Food f WHERE f.source = 'SYSTEM' AND f.active = true ORDER BY f.name")
    List<Food> findAllSystemFoods();

    @Query("SELECT f FROM Food f WHERE f.dietitian.id = :dietitianId AND f.active = true ORDER BY f.name")
    List<Food> findAllByDietitianId(UUID dietitianId);

    @Query("SELECT DISTINCT f.category FROM Food f WHERE f.active = true AND f.category IS NOT NULL ORDER BY f.category")
    List<String> findAllCategories();
}
