package org.sellhelp.backend.security;

import jakarta.servlet.http.Cookie;

public class CookieGenerator {
    public static Cookie createCookie(String cookieName, String cookieValue, int cookieExpiration){
        Cookie cookie = new Cookie(cookieName, cookieValue);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath("/");
        cookie.setMaxAge(cookieExpiration);
        cookie.setAttribute("SameSite", "Strict");

        return cookie;
    }
}
