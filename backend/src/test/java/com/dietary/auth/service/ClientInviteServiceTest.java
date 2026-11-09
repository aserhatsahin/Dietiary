package com.dietary.auth.service;

import com.dietary.auth.domain.ClientInvite;
import com.dietary.auth.domain.User;
import com.dietary.auth.repository.ClientInviteRepository;
import com.dietary.auth.repository.UserRepository;
import com.dietary.client.controller.dto.ClientInviteDTO;
import com.dietary.client.domain.Client;
import com.dietary.client.repository.ClientRepository;
import com.dietary.common.exception.BadRequestException;
import com.dietary.common.exception.DuplicateResourceException;
import com.dietary.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClientInviteServiceTest {

    @Mock
    private ClientRepository clientRepository;
    @Mock
    private ClientInviteRepository clientInviteRepository;
    @Mock
    private UserRepository userRepository;

    private ClientInviteService service;

    private final UUID dietitianId = UUID.randomUUID();
    private final UUID clientId = UUID.randomUUID();
    private Client client;

    @BeforeEach
    void setUp() {
        service = new ClientInviteService(clientRepository, clientInviteRepository, userRepository, 7);
        client = Client.builder()
                .id(clientId)
                .fullName("Ayse Yilmaz")
                .email("ayse@example.com")
                .active(true)
                .build();
    }

    @Test
    void createsNewInviteForClientWithoutAccount() {
        when(clientRepository.findByIdAndDietitianId(clientId, dietitianId)).thenReturn(Optional.of(client));
        when(userRepository.existsByEmail("ayse@example.com")).thenReturn(false);
        when(clientInviteRepository.findByClientId(clientId)).thenReturn(Optional.empty());
        when(clientInviteRepository.save(any(ClientInvite.class))).thenAnswer(inv -> inv.getArgument(0));

        ClientInviteDTO result = service.createInvite(clientId, dietitianId);

        assertThat(result.getClientId()).isEqualTo(clientId);
        assertThat(result.getEmail()).isEqualTo("ayse@example.com");
        assertThat(result.getInviteToken()).hasSizeGreaterThanOrEqualTo(40);
        assertThat(result.getExpiresAt())
                .isAfter(Instant.now().plus(Duration.ofDays(6)))
                .isBefore(Instant.now().plus(Duration.ofDays(8)));
    }

    @Test
    void reinvitingReplacesTokenOnExistingRow() {
        ClientInvite existing = ClientInvite.builder()
                .id(UUID.randomUUID())
                .clientId(clientId)
                .email("old@example.com")
                .token("old-token")
                .expiresAt(Instant.now().minus(Duration.ofDays(1)))
                .build();
        when(clientRepository.findByIdAndDietitianId(clientId, dietitianId)).thenReturn(Optional.of(client));
        when(userRepository.existsByEmail("ayse@example.com")).thenReturn(false);
        when(clientInviteRepository.findByClientId(clientId)).thenReturn(Optional.of(existing));
        when(clientInviteRepository.save(any(ClientInvite.class))).thenAnswer(inv -> inv.getArgument(0));

        ClientInviteDTO result = service.createInvite(clientId, dietitianId);

        assertThat(result.getInviteToken()).isNotEqualTo("old-token");
        assertThat(existing.getToken()).isEqualTo(result.getInviteToken());
        assertThat(existing.getEmail()).isEqualTo("ayse@example.com");
        assertThat(existing.isExpired()).isFalse();
    }

    @Test
    void rejectsClientOfAnotherDietitian() {
        when(clientRepository.findByIdAndDietitianId(clientId, dietitianId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createInvite(clientId, dietitianId))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(clientInviteRepository, never()).save(any());
    }

    @Test
    void rejectsClientThatAlreadyHasAccount() {
        client.setUser(User.builder().id(UUID.randomUUID()).build());
        when(clientRepository.findByIdAndDietitianId(clientId, dietitianId)).thenReturn(Optional.of(client));

        assertThatThrownBy(() -> service.createInvite(clientId, dietitianId))
                .isInstanceOf(BadRequestException.class);
        verify(clientInviteRepository, never()).save(any());
    }

    @Test
    void rejectsInactiveClient() {
        client.setActive(false);
        when(clientRepository.findByIdAndDietitianId(clientId, dietitianId)).thenReturn(Optional.of(client));

        assertThatThrownBy(() -> service.createInvite(clientId, dietitianId))
                .isInstanceOf(BadRequestException.class);
        verify(clientInviteRepository, never()).save(any());
    }

    @Test
    void rejectsWhenEmailAlreadyBelongsToAUser() {
        when(clientRepository.findByIdAndDietitianId(clientId, dietitianId)).thenReturn(Optional.of(client));
        when(userRepository.existsByEmail("ayse@example.com")).thenReturn(true);

        assertThatThrownBy(() -> service.createInvite(clientId, dietitianId))
                .isInstanceOf(DuplicateResourceException.class);
        verify(clientInviteRepository, never()).save(any());
    }
}
