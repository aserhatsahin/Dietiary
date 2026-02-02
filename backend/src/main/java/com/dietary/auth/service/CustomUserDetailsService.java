package com.dietary.auth.service;

import com.dietary.auth.domain.User;
import com.dietary.auth.repository.UserRepository;
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

        return UserPrincipal.create(user);
    }
}
