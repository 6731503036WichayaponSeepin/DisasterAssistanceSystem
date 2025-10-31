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

    private static final String SECRET = "MySuperSecretKeyForJWTGeneration12345"; // ควรเก็บใน ENV จริง ๆ
    private static final long EXPIRATION = 1000 * 60 * 60; // 1 ชั่วโมง

    private final Key key = Keys.hmacShaKeyFor(SECRET.getBytes());

    // ✅ สร้าง token
    public String generateToken(String rescueId, String role) {
        return Jwts.builder()
                .setSubject(rescueId)
                .claim("role", role)
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