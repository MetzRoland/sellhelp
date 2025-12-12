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

    @Value("${jwt_secret}")
    private String secret;

    public String generateToken(String email) throws
            IllegalArgumentException, JWTCreationException {
        return JWT.create()
                .withSubject("User Details")
                .withClaim("email", email)
                .withIssuedAt(new Date())
                .withIssuer("SellHelp")
                .withExpiresAt(new Date(System.currentTimeMillis() + 1000 * 60 * 15))
                .sign(Algorithm.HMAC256(secret));
    }

    public String validateTokenAndRetrieveSubject(String token)
            throws JWTVerificationException
    {
        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secret))
                .withSubject("User Details")
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

//    public boolean validateToken(String token, UserDetails userDetails) {
//        try {
//            String email = extractEmail(token);
//            return (email != null && email.equals(userDetails.getUsername())
//                    && !isTokenExpired(token));
//        } catch (Exception e) {
//            return false;
//        }
//    }

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
            return decodedJWT.getSignature().equals(Algorithm.HMAC256(secret).toString());
        } catch (Exception e) {
            // Could log the error here
        }
        return false;
    }

//    public boolean validateToken(String token, UserDetails userDetails) {
//        try {
//            String email = extractEmail(token);
//            return (email != null && email.equals(userDetails.getUsername())
//                    && !isTokenExpired(token) && isValidSigniture(token));
//        } catch (Exception e) {
//            return false;
//        }
//    }

    public boolean validateToken(String token, UserDetails userDetails) {
        try {
            JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secret))
                    .withSubject("User Details")
                    .withIssuer("SellHelp")
                    .build();

            DecodedJWT jwt = verifier.verify(token); // throws exception if invalid
            String email = jwt.getClaim("email").asString();

            return email != null && email.equals(userDetails.getUsername());
        } catch (JWTVerificationException e) {
            return false;
        }
    }

}