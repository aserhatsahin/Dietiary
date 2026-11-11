package com.dietary.common.security;

import com.dietary.auth.domain.Role;
import com.dietary.auth.domain.User;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtTokenProviderTest {

    private static final String STRONG_SECRET = "a-test-secret-that-is-long-enough-0123456789";

    private static JwtTokenProvider provider(String secret) {
        return new JwtTokenProvider(secret, 60_000, 600_000);
    }

    @Test
    void refusesToStartWithAShortSecret() {
        assertThatThrownBy(() -> provider("too-short"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("at least 32 bytes");
    }

    @Test
    void refusesToStartWithoutASecret() {
        assertThatThrownBy(() -> provider(null)).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> provider("")).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void issuedTokenValidatesAndCarriesTheUserId() {
        UUID userId = UUID.randomUUID();
        User user = User.builder().id(userId).email("a@example.test").fullName("A").role(Role.DIETITIAN)
                .passwordHash("x").build();
        JwtTokenProvider jwt = provider(STRONG_SECRET);

        String token = jwt.generateAccessToken(UserPrincipal.create(user));

        assertThat(jwt.validateToken(token)).isTrue();
        assertThat(jwt.getUserIdFromToken(token)).isEqualTo(userId);
    }

    @Test
    void tokenSignedWithAnotherSecretIsRejected() {
        User user = User.builder().id(UUID.randomUUID()).email("a@example.test").fullName("A")
                .role(Role.DIETITIAN).passwordHash("x").build();
        String foreign = provider("another-secret-that-is-also-long-enough-987654")
                .generateAccessToken(UserPrincipal.create(user));

        assertThat(provider(STRONG_SECRET).validateToken(foreign)).isFalse();
    }
}
