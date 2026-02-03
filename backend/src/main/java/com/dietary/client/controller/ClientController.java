package com.dietary.client.controller;

import com.dietary.auth.domain.User;
import com.dietary.auth.repository.UserRepository;
import com.dietary.client.controller.dto.ClientDTO;
import com.dietary.client.controller.dto.ClientRequest;
import com.dietary.client.service.ClientService;
import com.dietary.common.dto.ApiResponse;
import com.dietary.common.exception.ResourceNotFoundException;
import com.dietary.common.security.CurrentUser;
import com.dietary.common.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/clients")
@RequiredArgsConstructor
@Tag(name = "Clients", description = "Client management endpoints for dietitians")
@SecurityRequirement(name = "bearerAuth")
public class ClientController {

    private final ClientService clientService;
    private final UserRepository userRepository;

    @GetMapping
    @PreAuthorize("hasRole('DIETITIAN')")
    @Operation(summary = "Get all clients", description = "Retrieves all non-deleted clients for the authenticated dietitian")
    public ResponseEntity<ApiResponse<List<ClientDTO>>> getAllClients(
            @CurrentUser UserPrincipal currentUser) {
        List<ClientDTO> clients = clientService.getAllClients(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(clients));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('DIETITIAN')")
    @Operation(summary = "Get client by ID", description = "Retrieves a specific client by ID for the authenticated dietitian")
    public ResponseEntity<ApiResponse<ClientDTO>> getClientById(
            @PathVariable UUID id,
            @CurrentUser UserPrincipal currentUser) {
        ClientDTO client = clientService.getClientById(id, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(client));
    }

    @PostMapping
    @PreAuthorize("hasRole('DIETITIAN')")
    @Operation(summary = "Create a new client", description = "Creates a new client profile for the authenticated dietitian")
    public ResponseEntity<ApiResponse<ClientDTO>> createClient(
            @Valid @RequestBody ClientRequest request,
            @CurrentUser UserPrincipal currentUser) {
        User dietitian = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUser.getId()));
        ClientDTO client = clientService.createClient(request, dietitian);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Client created successfully", client));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('DIETITIAN')")
    @Operation(summary = "Update a client", description = "Updates an existing client profile for the authenticated dietitian")
    public ResponseEntity<ApiResponse<ClientDTO>> updateClient(
            @PathVariable UUID id,
            @Valid @RequestBody ClientRequest request,
            @CurrentUser UserPrincipal currentUser) {
        ClientDTO client = clientService.updateClient(id, request, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Client updated successfully", client));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('DIETITIAN')")
    @Operation(summary = "Delete a client", description = "Soft deletes a client profile for the authenticated dietitian")
    public ResponseEntity<ApiResponse<Void>> deleteClient(
            @PathVariable UUID id,
            @CurrentUser UserPrincipal currentUser) {
        clientService.deleteClient(id, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Client deleted successfully"));
    }

    @PatchMapping("/{id}/toggle-active")
    @PreAuthorize("hasRole('DIETITIAN')")
    @Operation(summary = "Toggle client active status", description = "Toggles the active status of a client")
    public ResponseEntity<ApiResponse<ClientDTO>> toggleClientActive(
            @PathVariable UUID id,
            @CurrentUser UserPrincipal currentUser) {
        ClientDTO client = clientService.toggleClientActive(id, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Client status updated", client));
    }
}
