package th.mfu.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import th.mfu.security.JwtAuthFilter;

@Configuration
public class SecurityConfig {

    @Autowired
    private JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            .cors(c -> c.configurationSource(corsConfigurationSource()))
            .csrf(c -> c.disable())

            .authorizeHttpRequests(auth -> auth

                /* 🟩 PUBLIC (ไม่ต้องล็อกอิน) */
                .requestMatchers(
                    "/api/users/login",
                    "/api/users/register",
                    "/api/rescues/login",
                    "/api/rescues/register",
                    "/api/cases/ping",
                    "/api/units"
                ).permitAll()

                /* 🟩 STATIC */
                .requestMatchers(
                    "/", "/index.html",
                    "/css/**", "/js/**",
                    "/images/**", "/assets/**", "/static/**"
                ).permitAll()

                /* 🟧 COMMON API (ทุก role ใช้ได้) */
                .requestMatchers("/api/location/**")
                    .hasAnyAuthority("ROLE_USER", "ROLE_RESCUE")

                /* =========================================================
                 * 🟥 RESCUE ONLY (อยู่ก่อน USER เสมอ!!!)
                 * ========================================================= */

                // 🚨 Rescue ดูรายการเคส (ทีมตัวเอง)
                .requestMatchers(HttpMethod.GET, "/api/cases/my")
                    .hasAuthority("ROLE_RESCUE")

                // 🚨 Rescue ดูเคสว่าง
                .requestMatchers(HttpMethod.GET, "/api/cases/available")
                    .hasAuthority("ROLE_RESCUE")

                // 🚨 Rescue ดูสถานะเคสละเอียด
                .requestMatchers(HttpMethod.GET, "/api/cases/status/**")
                    .hasAuthority("ROLE_RESCUE")

                // 🚨 Rescue เลือกเคส (หน้ามือถือ)
                .requestMatchers("/api/case-selection/**")
                    .hasAuthority("ROLE_RESCUE")

                // 🚨 Rescue กด follow / coming / done
                .requestMatchers(HttpMethod.POST, "/api/cases/*/follow").hasAuthority("ROLE_RESCUE")
                .requestMatchers(HttpMethod.POST, "/api/cases/*/coming").hasAuthority("ROLE_RESCUE")
                .requestMatchers(HttpMethod.POST, "/api/cases/*/done").hasAuthority("ROLE_RESCUE")

                // 🚨 Rescue API ที่เกี่ยวข้องกับ account rescue
                .requestMatchers("/api/rescues/**").hasAuthority("ROLE_RESCUE")
                .requestMatchers("/api/rescue/**").hasAuthority("ROLE_RESCUE")
                .requestMatchers("/api/rescue-teams/**").hasAuthority("ROLE_RESCUE")
                .requestMatchers("/api/rescues/avaliable").hasAuthority("ROLE_RESCUE")
                .requestMatchers(HttpMethod.GET, "/api/cases/*")
                                .hasAnyAuthority("ROLE_RESCUE", "ROLE_USER")
                /* =========================================================
                 * 🟦 USER ONLY 
                 * ========================================================= */

                // 👤 User ดู active case ของตัวเอง
                .requestMatchers(HttpMethod.GET, "/api/cases/my-active")
                    .hasAuthority("ROLE_USER")

                // 👤 User ดู latest (ถ้าระบบใช้)
                .requestMatchers(HttpMethod.GET, "/api/cases/latest")
                    .hasAuthority("ROLE_USER")

                // 👤 User ดึงรายละเอียดเคสของตัวเอง (timeline)
                .requestMatchers(HttpMethod.GET, "/api/cases/*")
                    .hasAuthority("ROLE_USER")

                // 👤 User ส่ง SOS / SUSTENANCE
                .requestMatchers(HttpMethod.POST, "/api/cases/report")
                    .hasAuthority("ROLE_USER")

                // 👤 User account
                .requestMatchers("/api/users/**").hasAuthority("ROLE_USER")
                .requestMatchers("/api/address/**").hasAuthority("ROLE_USER")
                .requestMatchers("/api/user-location/**").hasAuthority("ROLE_USER")

                /* =========================================================
                 * OTHER (ต้องล็อกอิน)
                 * ========================================================= */
                .anyRequest().authenticated()
            )

            .sessionManagement(sess ->
                sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOrigin("http://localhost:5173");
        config.addAllowedOrigin("http://127.0.0.1:5173");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
