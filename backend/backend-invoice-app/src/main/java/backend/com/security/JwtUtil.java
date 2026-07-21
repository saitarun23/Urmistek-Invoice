package backend.com.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * Small wrapper around jjwt. The token carries:
 *  - sub: username
 *  - companyCode: which tenant this login belongs to (URMISTEK / UB_INDUSTRIES)
 *  - companyId, role
 * Every protected endpoint reads companyId back off the token so a
 * URMISTEK login can never see or create UB Industries invoices, and
 * vice versa.
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration-ms:28800000}") // default 8 hours
    private long expirationMs;

    private SecretKey key() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateToken(String username, String companyCode, Long companyId, String role) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);
        return Jwts.builder()
                .setSubject(username)
                .claim("companyCode", companyCode)
                .claim("companyId", companyId)
                .claim("role", role)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean isValid(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
