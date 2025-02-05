package hu.martin.ems.core.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;

public class JwtUtil {
    private String secretKey = "mySecretKey";  // Erősebb titkos kulcs szükséges egy éles rendszerben

    // Token generálása
    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))  // 1 óra
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    // Token validálása
    public boolean validateToken(String token, String username) {
        String extractedUsername = extractUsername(token);
        return (extractedUsername.equals(username) && !isTokenExpired(token));
    }

    // Felhasználónév kinyerése a tokenből
    public String extractUsername(String token) {
        return extractClaims(token).getSubject();
    }

    // A tokenből a claim-ek kinyerése
    private Claims extractClaims(String token) {
        return Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJwt(token)
                .getBody();
    }

    // Token lejáratának ellenőrzése
    private boolean isTokenExpired(String token) {
        return extractClaims(token).getExpiration().before(new Date());
    }
}
