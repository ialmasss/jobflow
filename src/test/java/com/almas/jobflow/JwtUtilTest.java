package com.almas.jobflow;

import com.almas.jobflow.security.JwtUtil;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilTest {

    private static final String SECRET = "test-secret-test-secret-test-secret-123456";
    private static final String OTHER_SECRET = "another-secret-another-secret-another-123456";

    private final JwtUtil jwtUtil = new JwtUtil(SECRET, 60_000);

    @Test
    void generatedTokenIsValidAndContainsEmail() {
        String token = jwtUtil.generateToken("alice@test.com");

        assertThat(jwtUtil.isTokenValid(token)).isTrue();
        assertThat(jwtUtil.extractEmail(token)).isEqualTo("alice@test.com");
    }

    @Test
    void garbageTokenIsInvalid() {
        assertThat(jwtUtil.isTokenValid("abc")).isFalse();
    }

    @Test
    void tokenSignedWithAnotherSecretIsInvalid() {
        String forged = new JwtUtil(OTHER_SECRET, 60_000).generateToken("alice@test.com");

        assertThat(jwtUtil.isTokenValid(forged)).isFalse();
    }

    @Test
    void expiredTokenIsInvalid() {
        String expired = new JwtUtil(SECRET, -1_000).generateToken("alice@test.com");

        assertThat(jwtUtil.isTokenValid(expired)).isFalse();
    }
}