package com.dietary.goal.domain;

import com.dietary.client.domain.Client;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "goals")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Goal {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @Enumerated(EnumType.STRING)
    @Column(name = "goal_type", nullable = false)
    private GoalType goalType;

    @Column(name = "target_weight_kg", precision = 5, scale = 2)
    private BigDecimal targetWeightKg;

    @Enumerated(EnumType.STRING)
    @Column(name = "activity_level", nullable = false)
    private ActivityLevel activityLevel;

    @Column(name = "bmr", nullable = false)
    private Integer bmr;

    @Column(name = "tdee", nullable = false)
    private Integer tdee;

    @Column(name = "daily_calorie_target", nullable = false)
    private Integer dailyCalorieTarget;

    @Column(name = "protein_grams")
    private Integer proteinGrams;

    @Column(name = "carbs_grams")
    private Integer carbsGrams;

    @Column(name = "fat_grams")
    private Integer fatGrams;

    @Column(name = "weekly_weight_change_kg", precision = 4, scale = 2)
    private BigDecimal weeklyWeightChangeKg;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
