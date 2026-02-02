package com.dietary.mealplan.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "meal_options")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MealOption {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meal_id", nullable = false)
    private Meal meal;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "total_calories")
    private Integer totalCalories;

    @Column(name = "display_order")
    private Integer displayOrder;

    @Builder.Default
    @OneToMany(mappedBy = "mealOption", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MealOptionItem> items = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public void addItem(MealOptionItem item) {
        items.add(item);
        item.setMealOption(this);
    }

    public void removeItem(MealOptionItem item) {
        items.remove(item);
        item.setMealOption(null);
    }
}
