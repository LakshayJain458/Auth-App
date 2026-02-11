package org.example.authappbackened.configs;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.authappbackened.entities.RefreshToken;
import org.example.authappbackened.entities.User;
import org.example.authappbackened.entities.enums.Provider;
import org.example.authappbackened.jwt.CookieService;
import org.example.authappbackened.jwt.JwtService;
import org.example.authappbackened.repositories.RefreshTokenRepo;
import org.example.authappbackened.repositories.UserRepo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OAuthAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepo userRepo;
    private final JwtService jwtService;
    private final RefreshTokenRepo refreshTokenRepo;
    private final CookieService cookieService;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    @Override
    @Transactional
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
        var user = getUser(authentication);

        User currUser = userRepo.findByEmail(user.getEmail());
        if (currUser == null) {
            User existingUserWithSameName = userRepo.findByUsername(user.getName());
            if (existingUserWithSameName != null) {
                user.setUsername(user.getName() + "_" + user.getProvider().name().toLowerCase());
            }
            currUser = userRepo.saveAndFlush(user);
        }

        String jwtId = UUID.randomUUID().toString();
        var refreshTokenDb = RefreshToken.builder()
                .jwtId(jwtId)
                .user(currUser)
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(jwtService.getRefreshTtlSeconds()))
                .revoked(false)
                .build();
        refreshTokenRepo.save(refreshTokenDb);
        String accessToken = jwtService.generateAccessToken(currUser);
        String refreshToken = jwtService.generateRefreshToken(currUser, refreshTokenDb.getJwtId());
        cookieService.attachRefreshTokenCookie(response, refreshToken, (int) jwtService.getRefreshTtlSeconds());

        // Redirect to frontend with access token
        String redirectUrl = frontendUrl + "/oauth/callback?token="
                + URLEncoder.encode(accessToken, StandardCharsets.UTF_8);
        response.sendRedirect(redirectUrl);
    }

    private static User getUser(Authentication authentication) {
        var oAuth2AuthenticationToken = (OAuth2AuthenticationToken) authentication;
        String registrationId = oAuth2AuthenticationToken.getAuthorizedClientRegistrationId();
        var oauth = (DefaultOAuth2User) authentication.getPrincipal();

        User user = new User();
        user.setEnabled(true);
        user.setPassword("OAUTH2_USER_PASSWORD");

        if (registrationId.equalsIgnoreCase("google")) {
            user.setEmail(oauth.getAttribute("email"));
            user.setUsername(oauth.getAttribute("name"));
            user.setProvider(Provider.GOOGLE);
            user.setProviderId(oauth.getAttribute("sub"));
            user.setImage(oauth.getAttribute("picture"));
        } else if (registrationId.equalsIgnoreCase("github")) {
            String email = oauth.getAttribute("email");
            if (email == null) {
                email = oauth.getAttribute("login") + "@github.com";
            }
            user.setEmail(email);
            user.setUsername(
                    oauth.getAttribute("name") != null ? oauth.getAttribute("name") : oauth.getAttribute("login"));
            user.setProvider(Provider.GITHUB);
            user.setProviderId(Objects.requireNonNull(oauth.getAttribute("id")).toString());
            user.setImage(oauth.getAttribute("avatar_url"));
        }
        return user;
    }
}
