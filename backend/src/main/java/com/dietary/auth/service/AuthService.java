package com.dietary.auth.service;

import com.dietary.auth.controller.dto.AcceptInviteRequest;
import com.dietary.auth.controller.dto.AuthResponse;
import com.dietary.auth.controller.dto.LoginRequest;
import com.dietary.auth.controller.dto.RegisterRequest;
import com.dietary.auth.domain.ClientInvite;
import com.dietary.auth.domain.RefreshToken;
import com.dietary.auth.domain.Role;
import com.dietary.auth.domain.User;
import com.dietary.auth.repository.ClientInviteRepository;
import com.dietary.auth.repository.RefreshTokenRepository;
import com.dietary.auth.repository.UserRepository;
import com.dietary.client.domain.Client;
import com.dietary.client.repository.ClientRepository;
import com.dietary.common.exception.BadRequestException;
import com.dietary.common.exception.DuplicateResourceException;
import com.dietary.common.exception.ResourceNotFoundException;
import com.dietary.common.exception.TokenException;
import com.dietary.common.security.JwtTokenProvider;
import com.dietary.common.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final ClientInviteRepository clientInviteRepository;
    private final ClientRepository clientRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    @Value("${jwt.access-token-expiration-ms}")
    private long accessTokenExpirationMs;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("User", "email", request.getEmail());
        }

        // Create new dietitian user
        User user = User.builder()
                .email(request.getEmail().toLowerCase().trim())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName().trim())
                .role(Role.DIETITIAN)
                .build();

        user = userRepository.save(user);
        log.info("Registered new dietitian: {}", user.getEmail());

        return generateAuthResponse(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        // Authenticate user
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail().toLowerCase().trim(),
                        request.getPassword()));

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userPrincipal.getId()));

        log.info("User logged in: {}", user.getEmail());

        return generateAuthResponse(user);
    }

    @Transactional
    public AuthResponse refreshToken(String refreshTokenValue) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(() -> new TokenException("Invalid refresh token"));

        if (refreshToken.isExpired()) {
            refreshTokenRepository.delete(refreshToken);
            throw new TokenException("Refresh token has expired. Please login again.");
        }

        User user = refreshToken.getUser();

        // Delete old refresh token and create new one
        refreshTokenRepository.delete(refreshToken);

        log.info("Token refreshed for user: {}", user.getEmail());

        return generateAuthResponse(user);
    }

    @Transactional
    public AuthResponse acceptInvite(AcceptInviteRequest request) {
        // Find the invite
        ClientInvite invite = clientInviteRepository.findByToken(request.getInviteToken())
                .orElseThrow(() -> new TokenException("Invalid invite token"));

        // Check if invite is expired
        if (invite.isExpired()) {
            throw new TokenException("Invite token has expired. Please request a new invite from your dietitian.");
        }

        // Check if invite is already accepted
        if (invite.isAccepted()) {
            throw new BadRequestException("This invite has already been used. Please login instead.");
        }

        // Check if email is already registered as a user
        if (userRepository.existsByEmail(invite.getEmail())) {
            throw new DuplicateResourceException(
                    "A user account already exists with this email. Please login instead.");
        }

        // Find the client profile
        Client client = clientRepository.findById(invite.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException("Client", "id", invite.getClientId()));

        // Create new client user account
        User user = User.builder()
                .email(invite.getEmail().toLowerCase().trim())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(client.getFullName())
                .role(Role.CLIENT)
                .build();

        user = userRepository.save(user);

        // Link user to client profile
        client.setUser(user);
        clientRepository.save(client);

        // Mark invite as accepted
        invite.setAcceptedAt(Instant.now());
        clientInviteRepository.save(invite);

        log.info("Client accepted invite and created account: {}", user.getEmail());

        return generateAuthResponse(user);
    }

    private AuthResponse generateAuthResponse(User user) {
        UserPrincipal userPrincipal = UserPrincipal.create(user);

        // Generate access token
        String accessToken = jwtTokenProvider.generateAccessToken(userPrincipal);

        // Generate and save refresh token
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(jwtTokenProvider.generateRefreshTokenValue())
                .expiresAt(Instant.now().plus(jwtTokenProvider.getRefreshTokenExpirationMs(), ChronoUnit.MILLIS))
                .build();

        refreshTokenRepository.save(refreshToken);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .tokenType("Bearer")
                .expiresIn(accessTokenExpirationMs / 1000) // Convert to seconds
                .user(AuthResponse.UserInfo.builder()
                        .id(user.getId().toString())
                        .email(user.getEmail())
                        .fullName(user.getFullName())
                        .role(user.getRole().name())
                        .build())
                .build();
    }
}
