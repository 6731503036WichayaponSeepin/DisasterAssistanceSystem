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
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())

            .authorizeHttpRequests(auth -> auth

                /* =============================
                 * PUBLIC
                 * ============================= */
                .requestMatchers(
                    "/api/users/login",
                    "/api/users/register",
                    "/api/rescues/login",
                    "/api/rescues/register",
                    "/api/cases/ping",
                    "/api/units"
                ).permitAll()

                /* =============================
                 * STATIC FILES
                 * ============================= */
                .requestMatchers(
                    "/", "/index.html",
                    "/css/**", "/js/**",
                    "/images/**", "/assets/**", "/static/**"
                ).permitAll()

                /* =============================
                 * LOCATION API (USER + RESCUE)
                 * ============================= */
                .requestMatchers("/api/location/**")
                    .hasAnyAuthority("ROLE_USER", "ROLE_RESCUE")

                /* =============================
                 * USER ONLY (SOS PAGE)
                 * ============================= */
                .requestMatchers("/api/users/**").hasAuthority("ROLE_USER")
                .requestMatchers("/api/address/**").hasAuthority("ROLE_USER")
                .requestMatchers("/api/user-location/**").hasAuthority("ROLE_USER")

                .requestMatchers(HttpMethod.GET, "/api/cases/my-active").hasAuthority("ROLE_USER")
                .requestMatchers(HttpMethod.GET, "/api/cases/*").hasAuthority("ROLE_USER")
                .requestMatchers(HttpMethod.POST, "/api/cases/report").hasAuthority("ROLE_USER")

                /* =============================
                 * RESCUE ONLY
                 * ============================= */
                .requestMatchers("/api/rescues/**").hasAuthority("ROLE_RESCUE")
                .requestMatchers("/api/rescue/**").hasAuthority("ROLE_RESCUE")
                .requestMatchers("/api/rescue-teams/**").hasAuthority("ROLE_RESCUE")
                .requestMatchers("/api/rescues/avaliable").hasAuthority("ROLE_RESCUE")
                .requestMatchers("/api/case-selection/**").hasAuthority("ROLE_RESCUE")


                .requestMatchers(HttpMethod.GET, "/api/cases/my").hasAuthority("ROLE_RESCUE")
                .requestMatchers(HttpMethod.GET, "/api/cases/available").hasAuthority("ROLE_RESCUE")

                .requestMatchers(HttpMethod.POST, "/api/cases/*/follow").hasAuthority("ROLE_RESCUE")
                .requestMatchers(HttpMethod.POST, "/api/cases/*/coming").hasAuthority("ROLE_RESCUE")
                .requestMatchers(HttpMethod.POST, "/api/cases/*/confirm").hasAuthority("ROLE_RESCUE")
                    .requestMatchers(HttpMethod.GET, "/api/cases").hasAuthority("ROLE_RESCUE")
                .requestMatchers(HttpMethod.GET, "/api/cases/status/**").hasAuthority("ROLE_RESCUE")
                .requestMatchers(HttpMethod.GET, "/api/cases/{id}").hasAuthority("ROLE_RESCUE")
                .requestMatchers(HttpMethod.POST, "/api/cases/{id}/coming").hasAuthority("ROLE_RESCUE")
                /* =============================
                 * BLOCK OTHERS
                 * ============================= */
                .anyRequest().denyAll()
            )

            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
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
