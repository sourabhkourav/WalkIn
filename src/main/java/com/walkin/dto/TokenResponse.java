package com.walkin.dto;
import java.time.Instant;
public record TokenResponse(String accessToken, String tokenType, Instant expiresAt) {}
