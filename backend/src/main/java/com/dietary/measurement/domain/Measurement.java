package com.dietary.measurement.domain;

import com.dietary.client.domain.Client;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "measurements")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Measurement {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @Column(name = "measurement_date", nullable = false)
    private LocalDate measurementDate;

    @Column(name = "weight_kg", precision = 5, scale = 2)
    private BigDecimal weightKg;

    @Column(name = "body_fat_percentage", precision = 5, scale = 2)
    private BigDecimal bodyFatPercentage;

    @Column(name = "muscle_mass_kg", precision = 5, scale = 2)
    private BigDecimal muscleMassKg;

    @Column(name = "water_percentage", precision = 5, scale = 2)
    private BigDecimal waterPercentage;

    @Column(name = "bone_mass_kg", precision = 5, scale = 2)
    private BigDecimal boneMassKg;

    @Column(name = "visceral_fat")
    private Integer visceralFat;

    @Column(name = "basal_metabolic_rate")
    private Integer basalMetabolicRate;

    @Column(name = "metabolic_age")
    private Integer metabolicAge;

    @Column(name = "bmi", precision = 5, scale = 2)
    private BigDecimal bmi;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
