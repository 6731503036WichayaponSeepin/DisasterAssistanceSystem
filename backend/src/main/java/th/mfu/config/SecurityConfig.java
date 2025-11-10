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
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                    // 1) เปิดให้ login / register ได้เลย
                    .requestMatchers(
                            "/api/users/login",
                            "/api/users/register",
                            "/api/rescue/login",
                            "/api/rescue/register",
                            "/api/cases/ping"
                    ).permitAll()

                    // 2) endpoint ที่ต้องล็อกอินเป็น user
                    .requestMatchers("/api/users/**", "/api/location/**").hasRole("USER")

                    // 3) endpoint ที่ต้องเป็นกู้ภัย
                    .requestMatchers("/api/rescue-teams/**", "/api/rescue/**").hasRole("RESCUE")

                    // 4) ✅ ของเรา: แจ้งเคสต้องล็อกอินก่อน
                    .requestMatchers("/api/cases/**").authenticated()

                    // 5) ที่เหลือค่อยว่ากัน
                    .anyRequest().permitAll()
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
