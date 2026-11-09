package com.dietary.auth.service;

import com.dietary.auth.domain.ClientInvite;
import com.dietary.auth.repository.ClientInviteRepository;
import com.dietary.auth.repository.UserRepository;
import com.dietary.client.controller.dto.ClientInviteDTO;
import com.dietary.client.domain.Client;
import com.dietary.client.repository.ClientRepository;
import com.dietary.common.exception.BadRequestException;
import com.dietary.common.exception.DuplicateResourceException;
import com.dietary.common.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

/**
 * Creates invite tokens that let a client set a password and get a CLIENT account.
 * A client has at most one invite row; inviting again replaces its token and expiry.
 */
@Slf4j
@Service
public class ClientInviteService {

    private static final int TOKEN_BYTES = 32;

    private final ClientRepository clientRepository;
    private final ClientInviteRepository clientInviteRepository;
    private final UserRepository userRepository;
    private final Duration inviteValidity;
    private final SecureRandom secureRandom = new SecureRandom();

    public ClientInviteService(ClientRepository clientRepository,
                               ClientInviteRepository clientInviteRepository,
                               UserRepository userRepository,
                               @Value("${app.invite.expiration-days:7}") long expirationDays) {
        this.clientRepository = clientRepository;
        this.clientInviteRepository = clientInviteRepository;
        this.userRepository = userRepository;
        this.inviteValidity = Duration.ofDays(expirationDays);
    }

    @Transactional
    public ClientInviteDTO createInvite(UUID clientId, UUID dietitianId) {
        Client client = clientRepository.findByIdAndDietitianId(clientId, dietitianId)
                .orElseThrow(() -> new ResourceNotFoundException("Client", "id", clientId));

        if (client.getUser() != null) {
            throw new BadRequestException("This client already has an account.");
        }
        if (!Boolean.TRUE.equals(client.getActive())) {
            throw new BadRequestException("Cannot invite an inactive client. Activate the client first.");
        }
        if (userRepository.existsByEmail(client.getEmail())) {
            throw new DuplicateResourceException(
                    "A user account already exists with the client's email: " + client.getEmail());
        }

        ClientInvite invite = clientInviteRepository.findByClientId(clientId)
                .orElseGet(() -> ClientInvite.builder().clientId(clientId).build());
        invite.setEmail(client.getEmail());
        invite.setToken(generateToken());
        invite.setExpiresAt(Instant.now().plus(inviteValidity));
        invite.setAcceptedAt(null);

        invite = clientInviteRepository.save(invite);
        log.info("Created invite for client '{}'", client.getId());

        return ClientInviteDTO.fromEntity(invite);
    }

    private String generateToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
