package com.dietary.food.domain;

import com.dietary.auth.domain.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "foods")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Food {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dietitian_id", nullable = false)
    private User dietitian;

    @Column(nullable = false)
    private String name;

    private String brand;

    @Column(name = "serving_size", nullable = false)
    private String servingSize;

    @Column(name = "serving_unit", nullable = false)
    private String servingUnit;

    @Column(name = "calories_per_serving", nullable = false)
    private Integer caloriesPerServing;

    @Column(name = "protein_grams", precision = 6, scale = 2)
    private BigDecimal proteinGrams;

    @Column(name = "carbs_grams", precision = 6, scale = 2)
    private BigDecimal carbsGrams;

    @Column(name = "fat_grams", precision = 6, scale = 2)
    private BigDecimal fatGrams;

    @Column(name = "fiber_grams", precision = 6, scale = 2)
    private BigDecimal fiberGrams;

    @Column(name = "sugar_grams", precision = 6, scale = 2)
    private BigDecimal sugarGrams;

    @Column(name = "sodium_mg", precision = 8, scale = 2)
    private BigDecimal sodiumMg;

    private String category;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Builder.Default
    @Column(nullable = false)
    private Boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
