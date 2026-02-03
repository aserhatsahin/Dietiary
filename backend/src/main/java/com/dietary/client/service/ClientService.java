package com.dietary.client.service;

import com.dietary.auth.domain.User;
import com.dietary.client.controller.dto.ClientDTO;
import com.dietary.client.controller.dto.ClientRequest;
import com.dietary.client.domain.Client;
import com.dietary.client.repository.ClientRepository;
import com.dietary.common.exception.DuplicateResourceException;
import com.dietary.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;

    @Transactional(readOnly = true)
    public List<ClientDTO> getAllClients(UUID dietitianId) {
        return clientRepository.findAllByDietitianId(dietitianId).stream()
                .map(ClientDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ClientDTO getClientById(UUID clientId, UUID dietitianId) {
        Client client = findClientByIdAndDietitianId(clientId, dietitianId);
        return ClientDTO.fromEntity(client);
    }

    @Transactional
    public ClientDTO createClient(ClientRequest request, User dietitian) {
        // Check if client with same email already exists for this dietitian
        String normalizedEmail = request.getEmail().toLowerCase().trim();
        if (clientRepository.existsByDietitianIdAndEmail(dietitian.getId(), normalizedEmail)) {
            throw new DuplicateResourceException("Client", "email", normalizedEmail);
        }

        Client client = Client.builder()
                .dietitian(dietitian)
                .fullName(request.getFullName().trim())
                .email(normalizedEmail)
                .phone(request.getPhone())
                .birthDate(request.getBirthDate())
                .heightCm(request.getHeightCm())
                .gender(request.getGender())
                .healthInfo(request.getHealthInfo())
                .active(true)
                .build();

        client = clientRepository.save(client);
        log.info("Created new client '{}' for dietitian '{}'", client.getFullName(), dietitian.getEmail());

        return ClientDTO.fromEntity(client);
    }

    @Transactional
    public ClientDTO updateClient(UUID clientId, ClientRequest request, UUID dietitianId) {
        Client client = findClientByIdAndDietitianId(clientId, dietitianId);

        // Check if email is being changed and if new email already exists
        String normalizedEmail = request.getEmail().toLowerCase().trim();
        if (!client.getEmail().equals(normalizedEmail) &&
                clientRepository.existsByDietitianIdAndEmail(dietitianId, normalizedEmail)) {
            throw new DuplicateResourceException("Client", "email", normalizedEmail);
        }

        client.setFullName(request.getFullName().trim());
        client.setEmail(normalizedEmail);
        client.setPhone(request.getPhone());
        client.setBirthDate(request.getBirthDate());
        client.setHeightCm(request.getHeightCm());
        client.setGender(request.getGender());
        client.setHealthInfo(request.getHealthInfo());

        client = clientRepository.save(client);
        log.info("Updated client '{}'", client.getFullName());

        return ClientDTO.fromEntity(client);
    }

    @Transactional
    public void deleteClient(UUID clientId, UUID dietitianId) {
        Client client = findClientByIdAndDietitianId(clientId, dietitianId);
        client.softDelete();
        clientRepository.save(client);
        log.info("Soft deleted client '{}'", client.getFullName());
    }

    @Transactional
    public ClientDTO toggleClientActive(UUID clientId, UUID dietitianId) {
        Client client = findClientByIdAndDietitianId(clientId, dietitianId);
        client.setActive(!client.getActive());
        client = clientRepository.save(client);
        log.info("Toggled client '{}' active status to {}", client.getFullName(), client.getActive());
        return ClientDTO.fromEntity(client);
    }

    private Client findClientByIdAndDietitianId(UUID clientId, UUID dietitianId) {
        return clientRepository.findByIdAndDietitianId(clientId, dietitianId)
                .orElseThrow(() -> new ResourceNotFoundException("Client", "id", clientId));
    }
}
