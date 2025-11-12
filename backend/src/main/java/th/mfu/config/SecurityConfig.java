package th.mfu.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import th.mfu.security.JwtAuthFilter;

// ⬇️ เพิ่มแค่บรรทัดนี้เพื่อใช้กำหนดสิทธิ์ตาม HTTP method
import org.springframework.http.HttpMethod;

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
                    "/api/rescues/login",
                    "/api/rescues/register",
                    // ⬇️ เคส: ping เปิดสาธารณะ
                    "/api/cases/ping"
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

                // ⬇️ เคส: ผู้ใช้แจ้งเคส
                .requestMatchers(HttpMethod.POST, "/api/cases/report").hasAuthority("ROLE_USER")

                // 🚒 RESCUE role
                .requestMatchers(
                    "/api/rescue/**",
                    "/api/rescue-teams/**"
                ).hasAuthority("ROLE_RESCUE")

                // ⬇️ เคส: ทีมกู้ภัยจัดการ/ดูเคสของตัวเอง/เคสว่าง
                .requestMatchers(HttpMethod.POST, "/api/cases/{id}/follow").hasAuthority("ROLE_RESCUE")
                .requestMatchers(HttpMethod.POST, "/api/cases/{id}/coming").hasAuthority("ROLE_RESCUE")
                .requestMatchers(HttpMethod.POST, "/api/cases/{id}/confirm").hasAuthority("ROLE_RESCUE")
                .requestMatchers(HttpMethod.GET,  "/api/cases/my").hasAuthority("ROLE_RESCUE")
                .requestMatchers(HttpMethod.GET,  "/api/cases/available").hasAuthority("ROLE_RESCUE")

                // ⬇️ (แนะนำ) ให้ RESCUE ดูรายการเคสทั้งหมดและตามสถานะได้
                .requestMatchers(HttpMethod.GET, "/api/cases").hasAuthority("ROLE_RESCUE")
                .requestMatchers(HttpMethod.GET, "/api/cases/status/**").hasAuthority("ROLE_RESCUE")

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
