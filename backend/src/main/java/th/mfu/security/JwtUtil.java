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

    private static final String SECRET = "MySuperSecretKeyForJWTGeneration12345"; 
    private static final long EXPIRATION = 1000 * 60 * 60;

    private final Key key = Keys.hmacShaKeyFor(SECRET.getBytes());

    // ✔ สร้าง token แบบถูกต้องตาม SecurityConfig
    public String generateToken(String identifier, String role) {

        // ถ้า role เป็น USER → แปลงเป็น ROLE_USER  
        // ถ้า role เป็น RESCUE → แปลงเป็น ROLE_RESCUE  
        String prefixedRole = role.startsWith("ROLE_") ? role : "ROLE_" + role;

        return Jwts.builder()
                .setSubject(identifier)
                .claim("role", prefixedRole)   // ⭐ ส่ง ROLE_USER / ROLE_RESCUE
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Jws<Claims> parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);
    }
}


