package th.mfu.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import th.mfu.security.JwtAuthFilter;
import org.springframework.http.HttpMethod;

@Configuration
public class SecurityConfig {

    @Autowired
    private JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            // ⭐ เปิด CORS พร้อมกำหนด config ของเราเอง
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // ⭐ ปิด CSRF (เพราะใช้ JWT)
            .csrf(csrf -> csrf.disable())

            // ⭐ RULE ทั้งหมด
            .authorizeHttpRequests(auth -> auth

                /* ====================================================
                 *  PUBLIC (เข้าได้เลย)
                 * ==================================================== */
                .requestMatchers(
                    "/api/users/login",
                    "/api/users/register",
                    "/api/rescues/login",
                    "/api/rescues/register",
                    "/api/cases/ping",
                    "/api/units"
                ).permitAll()

                /* ====================================================
                 *  STATIC FILES (เข้าได้เลย)
                 * ==================================================== */
                .requestMatchers(
                    "/",
                    "/index.html",
                    "/css/**",
                    "/js/**",
                    "/images/**",
                    "/assets/**",
                    "/static/**"
                ).permitAll()

                /* ====================================================
                 *  LOCATION API (ใช้ได้ทั้ง USER + RESCUE)
                 * ==================================================== */
                .requestMatchers("/api/location/**")
                    .hasAnyAuthority("ROLE_USER", "ROLE_RESCUE")

                /* ====================================================
                 *  USER ONLY
                 * ==================================================== */
                .requestMatchers(
                    "/api/users/**",
                    "/api/address/**",
                    "/api/user-location/**"
                ).hasAuthority("ROLE_USER")

                .requestMatchers(HttpMethod.POST, "/api/cases/report")
                    .hasAuthority("ROLE_USER")

                /* ====================================================
                 *  RESCUE ONLY
                 * ==================================================== */
                .requestMatchers(
                    "/api/rescues/**",
                    "/api/rescue/**",
                    "/api/rescues/avaliable"
                ).hasAuthority("ROLE_RESCUE")

                .requestMatchers("/api/rescue-teams/**")
                    .hasAuthority("ROLE_RESCUE")

                .requestMatchers(HttpMethod.POST, "/api/cases/{id}/follow").hasAuthority("ROLE_RESCUE")
                .requestMatchers(HttpMethod.POST, "/api/cases/{id}/coming").hasAuthority("ROLE_RESCUE")
                .requestMatchers(HttpMethod.POST, "/api/cases/{id}/confirm").hasAuthority("ROLE_RESCUE")

                .requestMatchers(HttpMethod.GET, "/api/cases/my").hasAuthority("ROLE_RESCUE")
                .requestMatchers(HttpMethod.GET, "/api/cases/available").hasAuthority("ROLE_RESCUE")
                .requestMatchers(HttpMethod.GET, "/api/cases").hasAuthority("ROLE_RESCUE")
                .requestMatchers(HttpMethod.GET, "/api/cases/status/**").hasAuthority("ROLE_RESCUE")

                /* ====================================================
                 *  อื่น ๆ ปฏิเสธทั้งหมด
                 * ==================================================== */
                .anyRequest().denyAll()
            )

            // ⭐ ใช้ JWT stateless mode
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // ⭐ JWT Filter แทน Username/Password แบบดั้งเดิม
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /* ====================================================
     *  GLOBAL CORS CONFIG (ใช้ที่ FE port 5173)
     * ==================================================== */
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
