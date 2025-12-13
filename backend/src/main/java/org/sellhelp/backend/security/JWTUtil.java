package org.sellhelp.backend.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JWTUtil {

    @Value("${jwt_access_secret}")
    private String access_secret;

    @Value("${jwt_refresh_secret}")
    private String refresh_secret;

    @Value("${jwt_access_time}")
    private int access_time;

    @Value("${jwt_refresh_time}")
    private int refresh_time;

    private String generateToken(String email, String secret, String tokenType, int expirationMS)
            throws IllegalArgumentException, JWTCreationException {
        return JWT.create()
                .withSubject("User Details")
                .withClaim("email", email)
                .withClaim("type", tokenType)
                .withIssuedAt(new Date())
                .withIssuer("SellHelp")
                .withExpiresAt(new Date(System.currentTimeMillis() + expirationMS))
                .sign(Algorithm.HMAC256(secret));
    }

    public String generateAccessToken(String email) {
        return generateToken(email, access_secret, "access", access_time);
    }

    public String generateRefreshToken(String email) {
        return generateToken(email, refresh_secret, "refresh", refresh_time);
    }

    public String validateTokenAndRetrieveSubject(String token)
            throws JWTVerificationException {
        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(access_secret))
                .withSubject("User Details")
                .withClaim("type", "access")
                .withIssuer("SellHelp")
                .build();

        DecodedJWT jwt = verifier.verify(token);

        return jwt.getClaim("email").asString();
    }

    public String extractEmail(String token) {
        try {
            DecodedJWT decodedJWT = JWT.decode(token);
            return decodedJWT.getClaim("email").asString();
        } catch (Exception e) {
            // Could log the error here
            return null;
        }
    }

    private boolean isTokenExpired(String token) {
        try {
            DecodedJWT decodedJWT = JWT.decode(token);
            Date expiry = decodedJWT.getExpiresAt();
            return expiry != null && expiry.before(new Date());
        } catch (Exception e) {
            return true; // treat invalid tokens as expired
        }
    }

    public boolean isValidSigniture(String token) {
        try {
            DecodedJWT decodedJWT = JWT.decode(token);
            return decodedJWT.getSignature().equals(Algorithm.HMAC256(access_secret).toString());
        } catch (Exception e) {
            // Could log the error here
        }
        return false;
    }

    public boolean validateToken(String token, String secret, String tokenType, UserDetails userDetails) {
        try {
            JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secret))
                    .withSubject("User Details")
                    .withClaim("type", tokenType)
                    .withIssuer("SellHelp")
                    .build();

            DecodedJWT jwt = verifier.verify(token); // throws exception if invalid
            String email = jwt.getClaim("email").asString();

            return email != null && email.equals(userDetails.getUsername());
        } catch (JWTVerificationException e) {
            return false;
        }
    }

    public boolean validateAccessToken(String token, UserDetails userDetails)
    {
        return validateToken(token, access_secret, "access", userDetails);
    }

    public boolean validateRefreshToken(String token, UserDetails userDetails)
    {
        return validateToken(token, refresh_secret, "refresh", userDetails);
    }

}
