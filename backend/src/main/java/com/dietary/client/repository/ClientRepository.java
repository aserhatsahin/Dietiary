package com.dietary.client.repository;

import com.dietary.client.domain.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClientRepository extends JpaRepository<Client, UUID> {

    @Query("SELECT c FROM Client c WHERE c.dietitian.id = :dietitianId AND c.deletedAt IS NULL")
    List<Client> findAllByDietitianId(UUID dietitianId);

    @Query("SELECT c FROM Client c WHERE c.id = :id AND c.dietitian.id = :dietitianId AND c.deletedAt IS NULL")
    Optional<Client> findByIdAndDietitianId(UUID id, UUID dietitianId);

    @Query("SELECT c FROM Client c WHERE c.user.id = :userId AND c.deletedAt IS NULL")
    Optional<Client> findByUserId(UUID userId);

    boolean existsByDietitianIdAndEmail(UUID dietitianId, String email);
}
