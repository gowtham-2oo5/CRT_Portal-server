package com.crt.server.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

/**
 * Service for managing HttpOnly cookies
 */
@Service
public class CookieService {

    @Value("${jwt.cookie.name:access_token}")
    private String jwtCookieName;

    @Value("${jwt.refresh.cookie.name:refresh_token}")
    private String refreshCookieName;

    @Value("${jwt.cookie.secure:false}")
    private boolean secureCookie;

    @Value("${jwt.cookie.domain:localhost}")
    private String cookieDomain;

    @Value("${jwt.cookie.path:/}")
    private String cookiePath;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    @Value("${jwt.refresh-token.expiration}")
    private long refreshExpiration;

    public void createAccessTokenCookie(String token, HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(jwtCookieName, token)
                .httpOnly(true)
                .secure(secureCookie)
                .domain(cookieDomain)
                .path(cookiePath)
                .maxAge(jwtExpiration / 1000)
                .sameSite("None")
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }

    public void createRefreshTokenCookie(String token, HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(refreshCookieName, token)
                .httpOnly(true)
                .secure(secureCookie)
                .domain(cookieDomain)
                .path(cookiePath)
                .maxAge(refreshExpiration / 1000)
                .sameSite("None")
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }


    public void clearAuthCookies(HttpServletResponse response) {
        ResponseCookie accessTokenCookie = ResponseCookie.from(jwtCookieName, "")
                .httpOnly(true)
                .secure(secureCookie)
                .domain(cookieDomain)
                .path(cookiePath)
                .maxAge(0)
                .sameSite("None")
                .build();
        response.addHeader("Set-Cookie", accessTokenCookie.toString());

        ResponseCookie refreshTokenCookie = ResponseCookie.from(refreshCookieName, "")
                .httpOnly(true)
                .secure(secureCookie)
                .domain(cookieDomain)
                .path(cookiePath)
                .maxAge(0)
                .sameSite("None")
                .build();
        response.addHeader("Set-Cookie", refreshTokenCookie.toString());
    }


    public String getJwtFromCookies(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (jwtCookieName.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }


    public String getRefreshTokenFromCookies(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (refreshCookieName.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}

