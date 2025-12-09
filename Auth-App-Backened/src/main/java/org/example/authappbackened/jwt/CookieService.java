package org.example.authappbackened.jwt;

import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

@Service
@Getter
public class CookieService {
    private final String refreshTokenCookieName;
    private final boolean cookieSecure;
    private final boolean cookieHttpOnly;
    private final String cookieSameSite;
    private final String cookieDomain;

    public CookieService(
            @Value("${security.jwt.refresh-token-cookie-name}") String refreshTokenCookieName,
            @Value("${security.jwt.cookie-secure}") boolean cookieSecure,
            @Value("${security.jwt.cookie-http-only}") boolean cookieHttpOnly,
            @Value("${security.jwt.cookie-same-site}") String cookieSameSite,
            @Value("${security.jwt.cookie.Domain}") String cookieDomain) {
        this.refreshTokenCookieName = refreshTokenCookieName;
        this.cookieSecure = cookieSecure;
        this.cookieHttpOnly = cookieHttpOnly;
        this.cookieSameSite = cookieSameSite;
        this.cookieDomain = cookieDomain;
    }

    public void attachRefreshTokenCookie(HttpServletResponse response, String refreshToken, int maxAge) {
        var responseCookieBuilder = ResponseCookie.from(refreshTokenCookieName, refreshToken)
                .httpOnly(cookieHttpOnly)
                .secure(cookieSecure)
                .path("/")
                .maxAge(maxAge)
                .sameSite(cookieSameSite);
        if (cookieDomain != null && !cookieDomain.isBlank()) {
            responseCookieBuilder.domain(cookieDomain);
        }
        ResponseCookie cookie = responseCookieBuilder.build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    public void clearRefreshTokenCookie(HttpServletResponse response) {
        var responseCookieBuilder = ResponseCookie.from(refreshTokenCookieName, "")
                .httpOnly(cookieHttpOnly)
                .secure(cookieSecure)
                .path("/")
                .maxAge(0)
                .sameSite(cookieSameSite);
        if (cookieDomain != null && !cookieDomain.isBlank()) {
            responseCookieBuilder.domain(cookieDomain);
        }
        ResponseCookie cookie = responseCookieBuilder.build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    public void addNoCacheHeaders(HttpServletResponse response) {
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
        response.setHeader(HttpHeaders.PRAGMA, "no-cache");
    }
}
