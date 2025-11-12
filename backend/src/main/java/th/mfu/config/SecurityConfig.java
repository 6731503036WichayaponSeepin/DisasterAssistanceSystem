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
            // ✅ ปิด CSRF เพราะใช้ JWT
            .csrf(csrf -> csrf.disable())

            // ✅ ตั้งค่าการอนุญาตการเข้าถึง
            .authorizeHttpRequests(auth -> auth

                // 🔓 Public endpoints (ไม่ต้อง login)
                .requestMatchers(
                    "/api/users/login",
                    "/api/users/register",
                    "/api/rescue/login",
                    "/api/rescue/register"
                ).permitAll()

                // 🔓 Static resources (HTML, CSS, JS)
                .requestMatchers(
                    "/", 
                    "/index.html",
                    "/css/**",
                    "/js/**",
                    "/images/**",
                    "/assets/**",
                    "/static/**"
                ).permitAll()

                // 👤 USER role
                .requestMatchers(
                    "/api/users/**",
                    "/api/address/**",
                    "/api/location/**"
                ).hasAuthority("ROLE_USER")

                // 🚒 RESCUE role
                .requestMatchers(
                    "/api/rescue/**",
                    "/api/rescue-teams/**"
                ).hasAuthority("ROLE_RESCUE")

                // ❌ ปฏิเสธทุก request อื่น
                .anyRequest().denyAll()
            )

            // ✅ Stateless session (JWT)
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // ✅ ใช้ JWT Filter ก่อน UsernamePasswordAuthenticationFilter
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
