package com.dietary.client.service;

import com.dietary.client.domain.Client;
import com.dietary.client.repository.ClientRepository;
import com.dietary.common.exception.ForbiddenException;
import com.dietary.common.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Resolves the client record behind a logged-in CLIENT user, so mobile endpoints can reuse
 * the dietitian-scoped services with the client's own (clientId, dietitianId) pair.
 */
@Service
@RequiredArgsConstructor
public class ClientContextService {

    private final ClientRepository clientRepository;

    public record ClientContext(UUID clientId, UUID dietitianId) {
    }

    @Transactional(readOnly = true)
    public ClientContext require(UserPrincipal principal) {
        UUID clientId = principal.getClientId();
        Client client = clientId == null ? null : clientRepository.findById(clientId)
                .filter(c -> !c.isDeleted())
                .orElse(null);
        if (client == null) {
            throw new ForbiddenException("No active client profile is linked to this account.");
        }
        return new ClientContext(client.getId(), client.getDietitian().getId());
    }
}
