package org.example.authappbackened.configs;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.authappbackened.entities.enums.ErrorResponse;
import org.example.authappbackened.jwt.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Slf4j
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    private final AuthenticationSuccessHandler successHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        log.info("Configuring BCryptPasswordEncoder");
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            final HttpSecurity http,
            final ObjectMapper objectMapper) throws Exception {

        log.info("Configuring security filter chain");
        http.csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorizeRequests ->
                        authorizeRequests
                                .requestMatchers(ApiConstant.Auth_Public_Endpoints).permitAll()
                                .anyRequest().authenticated())
                .oauth2Login(oauth2 -> oauth2
                        .successHandler(successHandler)
                        .failureHandler(null)
                )
                .logout(AbstractHttpConfigurer::disable)
                .exceptionHandling(ex ->
                        ex.authenticationEntryPoint((request, response, authException) ->
                                handleAuthenticationException(response, authException, objectMapper)))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .httpBasic(Customizer.withDefaults());

        log.info("Security filter chain configured successfully");
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    private void handleAuthenticationException(
            final HttpServletResponse response,
            final Exception authException,
            final ObjectMapper objectMapper) {

        try {
            log.warn("Authentication failed: {}", authException.getMessage());

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            final String errorMessage = "Unauthorized Access: " + authException.getMessage();
            final ErrorResponse error = new ErrorResponse(
                    errorMessage,
                    HttpStatus.UNAUTHORIZED,
                    HttpStatus.UNAUTHORIZED.value());

            response.getWriter().write(objectMapper.writeValueAsString(error));
            response.getWriter().flush();

        } catch (Exception e) {
            log.error("Error while handling authentication exception: {}", e.getMessage(), e);
        }
    }
}
