package org.sellhelp.backend.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
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

    public void generateLoginCookies(HttpServletResponse response, String accessTokenCookieValue,
                                     String refreshTokenCookieValue){
        Cookie accessTokenCookie = createAccessCookie(accessTokenCookieValue);
        Cookie refreshTokenCookie = createRefreshCookie(refreshTokenCookieValue);

        response.addCookie(accessTokenCookie);
        response.addCookie(refreshTokenCookie);
    }

    public void refreshAccessTokenCookie(HttpServletResponse response, String accessTokenCookieValue){
        Cookie accessTokenCookie = createAccessCookie(accessTokenCookieValue);

        response.addCookie(accessTokenCookie);
    }

    public void deleteLogoutCookies(HttpServletRequest request, HttpServletResponse response){
        Cookie accessTokenCookie = deleteCookie("accessToken");
        Cookie refreshTokenCookie = deleteCookie("refreshToken");
        Cookie jSessionIdCookie = deleteCookie("JSESSIONID");

        response.addCookie(accessTokenCookie);
        response.addCookie(refreshTokenCookie);
        response.addCookie(jSessionIdCookie);

        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        SecurityContextHolder.clearContext();
    }

    public Cookie createAccessCookie(String cookieValue)
    {
        return createCookie("accessToken", cookieValue, accessTokenCookieExpiration, "/");
    }

    public Cookie createRefreshCookie(String cookieValue)
    {
        return createCookie("refreshToken", cookieValue, refreshTokenCookieExpiration, "/");
    }

    public Cookie deleteCookie(String cookieName)
    {
        return createCookie(cookieName, null, 0, "/");
    }
}
