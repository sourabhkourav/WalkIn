package com.walkin.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RegistrationTokenServiceTests {

    private final RegistrationTokenService tokenService = new RegistrationTokenService();

    @Test
    void generatesIndependentUrlSafeTokensWith256BitsOfRandomness() {
        String first = tokenService.generateToken();
        String second = tokenService.generateToken();

        assertThat(first)
                .hasSize(43)
                .matches("[A-Za-z0-9_-]+");
        assertThat(second).isNotEqualTo(first);
    }

    @Test
    void hashesTokensDeterministicallyWithoutRetainingRawValue() {
        String hash = tokenService.hashToken("example-registration-token");

        assertThat(hash)
                .hasSize(64)
                .matches("[0-9a-f]+")
                .isEqualTo(tokenService.hashToken("example-registration-token"))
                .isNotEqualTo("example-registration-token");
        assertThat(tokenService.hashToken("different-token")).isNotEqualTo(hash);
    }
}
