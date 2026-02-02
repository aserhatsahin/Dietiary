package com.dietary.auth.repository;

import com.dietary.auth.domain.ClientInvite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClientInviteRepository extends JpaRepository<ClientInvite, UUID> {

    Optional<ClientInvite> findByToken(String token);

    Optional<ClientInvite> findByClientId(UUID clientId);

    boolean existsByClientIdAndAcceptedAtIsNull(UUID clientId);
}
