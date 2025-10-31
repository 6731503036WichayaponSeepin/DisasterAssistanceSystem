package th.mfu.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import th.mfu.security.JwtAuthFilter;

@Configuration
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // ✅ ไม่ต้องใช้ token ตอน login / register
                        .requestMatchers(
                                "/api/users/login",
                                "/api/users/register",
                                "/api/rescue/login",
                                "/api/rescue/register"
                        ).permitAll()

                        // ✅ USER ต้องมี token (JWT) และ role = USER เท่านั้น
                        .requestMatchers("/api/users/**").hasRole("USER")

                        // ✅ RESCUE ต้องมี token (JWT) และ role = RESCUE เท่านั้น
                        .requestMatchers("/api/rescue-teams/**", "/api/rescue/**").hasRole("RESCUE")

                        // ✅ ทุก path อื่นๆ เปิดให้เข้าได้
                        .anyRequest().permitAll()
                )

                // ✅ เพิ่ม filter ตรวจสอบ JWT ก่อน UsernamePasswordAuthenticationFilter
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}