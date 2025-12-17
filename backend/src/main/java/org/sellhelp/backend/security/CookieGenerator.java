package org.sellhelp.backend.security;

import jakarta.servlet.http.Cookie;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CookieGenerator {
    @Value("${jwt.cookie.access.time}")
    private int accessTokenCookieExpiration;

    @Value("${jwt.cookie.refresh.time}")
    private int refreshTokenCookieExpiration;

    public Cookie createCookie(String cookieName, String cookieValue, int cookieExpiration, String path){
        Cookie cookie = new Cookie(cookieName, cookieValue);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setMaxAge(cookieExpiration);
        cookie.setPath(path);
        cookie.setAttribute("SameSite", "Strict");

        return cookie;
    }

    public Cookie createAccessCookie(String cookieValue)
    {
        return createCookie("accessToken", cookieValue, accessTokenCookieExpiration, "/");
    }

    public Cookie createRefreshCookie(String cookieValue)
    {
        return createCookie("refreshToken", cookieValue, refreshTokenCookieExpiration, "/auth/login/refresh");
    }
}
