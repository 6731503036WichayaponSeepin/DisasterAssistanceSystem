package th.mfu.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import th.mfu.security.JwtAuthFilter;

@Configuration
public class SecurityConfig {

    @Autowired
    private JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // ✅ ปิด CSRF (เพราะเราจะใช้ JWT ไม่ได้ใช้ session form)
            .csrf(csrf -> csrf.disable())

            // ✅ กำหนดสิทธิ์การเข้าถึงของแต่ละ path
            .authorizeHttpRequests(auth -> auth
                    // 🔹 เปิดให้เข้าถึงได้โดยไม่ต้องมี token (เช่น login/register)
                    .requestMatchers(
                            "/api/users/login",
                            "/api/users/register",
                            "/api/rescue/login",
                            "/api/rescue/register"
                    ).permitAll()

                    // 🔹 เส้นทางของผู้ใช้ทั่วไป ต้อง role = USER
                    .requestMatchers("/api/users/**", "/api/location/**").hasRole("USER")

                    // 🔹 เส้นทางของกู้ภัย ต้อง role = RESCUE
                    .requestMatchers("/api/rescue-teams/**", "/api/rescue/**").hasRole("RESCUE")

                    // 🔹 อื่น ๆ เปิดให้เข้าได้ (หรือจะเปลี่ยนเป็น authenticated() ก็ได้)
                    .anyRequest().permitAll()
            )

            // ✅ ใช้ Stateless Session (เพราะ JWT ไม่มี session)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // ✅ เพิ่ม filter JWT ก่อน AuthenticationFilter ปกติ
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
