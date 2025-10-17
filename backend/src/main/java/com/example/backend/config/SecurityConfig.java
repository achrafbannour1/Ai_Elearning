package com.example.backend.config;

import com.example.backend.security.JwitFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityConfig {

    @Autowired
    private JwitFilter jwtFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // ✅ Préflight CORS
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // ✅ Auth publique
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()
                                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/student/**").hasRole("STUDENT")
                        .requestMatchers("/event/register/**").authenticated()


                                .requestMatchers("/api/payment/webhook").permitAll()
                        .requestMatchers("/api/subscription/**").authenticated() // Subscription endpoints
                        .requestMatchers("/api/payment/**").authenticated()   //
                        .requestMatchers("/event/**").permitAll()


                                .requestMatchers("/api/stripe/**").permitAll() //
//

                        .requestMatchers("/api/ollama/**").authenticated()
                        .requestMatchers("/images/**").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()

                        // ✅ Rôles
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/student/**").hasRole("STUDENT")

                        // ✅ Events
                        .requestMatchers("/event/register/**").authenticated()
                        .requestMatchers("/event/**").permitAll()

                        // ✅ Courses
                        .requestMatchers("/course/register/**").authenticated()
                        .requestMatchers("/course/**").permitAll()

                        // ✅ Lessons
                        .requestMatchers("/lesson/register/**").authenticated()

                        // ✅ IA
                        .requestMatchers("/ai/register/**").authenticated()
                        .requestMatchers("/ai/**").permitAll()

                        // ✅ OUVERTURE EXPLICITE CRUD LESSON (debug sans token)
                        .requestMatchers(HttpMethod.POST,   "/lesson/add-lesson/**").permitAll()
                        .requestMatchers(HttpMethod.PUT,    "/lesson/modify-lesson").permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/lesson/remove-lesson/**").permitAll()

                        // ✅ IA résumé (POST) ouvert pour tests
                        .requestMatchers(HttpMethod.POST, "/lesson/autosummary/**").permitAll()

                        // ✅ le reste des endpoints Lesson (GET, etc.)
                        .requestMatchers("/lesson/**").permitAll()

                        // Exemples d’API protégées
                        .requestMatchers("/api/ollama/**").authenticated()

                        // Tout le reste nécessite une auth
                        .anyRequest().authenticated()
                );

        // Filtre JWT (les routes permitAll passeront quand même)
        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:4200")); // ton Angular
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        // config.setExposedHeaders(List.of("Authorization")); // si besoin

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
