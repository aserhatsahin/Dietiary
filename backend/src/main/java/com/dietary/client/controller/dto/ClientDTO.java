package com.dietary.client.controller.dto;

import com.dietary.client.domain.Client;
import com.dietary.client.domain.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientDTO {

    private UUID id;
    private String fullName;
    private String email;
    private String phone;
    private LocalDate birthDate;
    private BigDecimal heightCm;
    private Gender gender;
    private String healthInfo;
    private Boolean active;
    private Boolean hasAccount;
    private Instant createdAt;
    private Instant updatedAt;

    public static ClientDTO fromEntity(Client client) {
        return ClientDTO.builder()
                .id(client.getId())
                .fullName(client.getFullName())
                .email(client.getEmail())
                .phone(client.getPhone())
                .birthDate(client.getBirthDate())
                .heightCm(client.getHeightCm())
                .gender(client.getGender())
                .healthInfo(client.getHealthInfo())
                .active(client.getActive())
                .hasAccount(client.getUser() != null)
                .createdAt(client.getCreatedAt())
                .updatedAt(client.getUpdatedAt())
                .build();
    }
}
