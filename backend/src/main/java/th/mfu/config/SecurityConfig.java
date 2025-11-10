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
                    // 1) เปิดให้เข้าได้โดยไม่ต้องมี token
                    .requestMatchers(
                            "/api/users/login",
                            "/api/users/register",
                            "/api/rescue/login",
                            "/api/rescue/register",
                            "/api/cases/ping"
                    ).permitAll()

                    // 2) user ทั่วไป
                    .requestMatchers("/api/users/**", "/api/location/**").hasRole("USER")

                    // 3) กลุ่มกู้ภัย
                    .requestMatchers("/api/rescue-teams/**", "/api/rescue/**").hasRole("RESCUE")
                    // 3.1 กู้ภัยทำกับเคส
                    .requestMatchers(
                            "/api/cases/*/follow",
                            "/api/cases/*/confirm",
                            "/api/cases/available",
                            "/api/cases/assigned-to/me"
                    ).hasRole("RESCUE")

                    // 4) ที่เป็น /api/cases อื่นๆ แค่ต้องล็อกอิน (report ก็อยู่ในนี้)
                    .requestMatchers("/api/cases/**").authenticated()

                    // 5) อื่นๆ ปล่อย
                    .anyRequest().permitAll()
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
