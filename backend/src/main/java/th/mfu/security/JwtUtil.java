package th.mfu.security;

import java.security.Key;
import java.util.Date;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {

    private static final String SECRET = "MySuperSecretKeyForJWTGeneration12345"; // ⚠️ ควรเก็บใน ENV จริง ๆ
    private static final long EXPIRATION = 1000 * 60 * 60; // 1 ชั่วโมง

    private final Key key = Keys.hmacShaKeyFor(SECRET.getBytes());

    // ✅ สร้าง token พร้อม role prefix "ROLE_"
    public String generateToken(String identifier, String role) {
        // Spring Security expects "ROLE_USER" or "ROLE_RESCUE"
        String prefixedRole = role.startsWith("ROLE_") ? role : "ROLE_" + role;

        return Jwts.builder()
                .setSubject(identifier) // phoneNumber หรือ rescueId
                .claim("role", prefixedRole)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // ✅ ดึงข้อมูลจาก token
    public Jws<Claims> parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);
    }
}
