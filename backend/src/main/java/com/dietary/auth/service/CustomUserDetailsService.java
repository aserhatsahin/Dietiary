package com.dietary.auth.service;

import com.dietary.auth.domain.Role;
import com.dietary.auth.domain.User;
import com.dietary.auth.repository.UserRepository;
import com.dietary.client.domain.Client;
import com.dietary.client.repository.ClientRepository;
import com.dietary.common.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final ClientRepository clientRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String usernameOrId) throws UsernameNotFoundException {
        User user;

        // Try to parse as UUID first (for JWT filter that uses user ID)
        try {
            UUID userId = UUID.fromString(usernameOrId);
            user = userRepository.findById(userId)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + usernameOrId));
        } catch (IllegalArgumentException e) {
            // Not a UUID, try as email
            user = userRepository.findByEmail(usernameOrId)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + usernameOrId));
        }

        // CLIENT users carry their client record id; the mobile endpoints read it from the principal.
        UUID clientId = null;
        if (user.getRole() == Role.CLIENT) {
            clientId = clientRepository.findByUserId(user.getId())
                    .map(Client::getId)
                    .orElse(null);
        }
        return UserPrincipal.create(user, clientId);
    }
}
