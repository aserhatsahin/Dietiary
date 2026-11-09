package com.dietary.client.controller.dto;

import com.dietary.auth.domain.ClientInvite;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientInviteDTO {

    private UUID clientId;
    private String email;
    private String inviteToken;
    private Instant expiresAt;

    public static ClientInviteDTO fromEntity(ClientInvite invite) {
        return ClientInviteDTO.builder()
                .clientId(invite.getClientId())
                .email(invite.getEmail())
                .inviteToken(invite.getToken())
                .expiresAt(invite.getExpiresAt())
                .build();
    }
}
