package th.mfu.security;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Autowired
    private final JwtUtil jwtUtil;

    public JwtAuthFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        // ✅ ตรวจว่า header มี Bearer token หรือไม่
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);

            try {
                // ✅ แปลง token เป็น claim
                Jws<Claims> claims = jwtUtil.parseToken(token);
                String subject = claims.getBody().getSubject(); // เช่น phoneNumber หรือ rescueId
                String role = claims.getBody().get("role", String.class);

                // ✅ ตรวจว่ามี subject และยังไม่มีการ auth ใน context
                if (subject != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                    // ✅ สร้าง authority จาก role ที่อ่านจาก token
                    List<SimpleGrantedAuthority> authorities =
                            List.of(new SimpleGrantedAuthority(role));

                    // ✅ สร้าง Authentication object สำหรับ Spring Security
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    new User(subject, "", authorities),
                                    null,
                                    authorities
                            );

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // ✅ ตั้งค่า Authentication ใน SecurityContext
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }

            } catch (Exception e) {
                // ❌ ถ้า token ผิด / หมดอายุ → ส่งกลับ 401
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"error\": \"Invalid or expired token\"}");
                return;
            }
        }

        // ✅ ส่งต่อให้ filter ถัดไป
        filterChain.doFilter(request, response);
    }
}
