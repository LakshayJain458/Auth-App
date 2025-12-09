package org.example.authappbackened.entities.enums;

import org.example.authappbackened.dtos.UserDto;

public record TokenResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        Long expiresIn,
        UserDto user
) {
    public static TokenResponse createTokenResponse(String accessToken, String refreshToken, Long expiresIn, UserDto user) {
        return new TokenResponse(accessToken, refreshToken, "Bearer", expiresIn, user);
    }
}
