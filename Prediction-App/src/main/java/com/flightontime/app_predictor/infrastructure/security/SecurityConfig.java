package com.flightontime.app_predictor.infrastructure.security;

import com.flightontime.app_predictor.infrastructure.config.CorrelationIdFilter;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Clase SecurityConfig.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final JwtTokenProvider jwtTokenProvider;
    private final CorrelationIdFilter correlationIdFilter;
    private final RequestObservabilityFilter requestObservabilityFilter;

    /**
     * Ejecuta la operación security config.
     * @param jwtTokenProvider variable de entrada jwtTokenProvider.
     * @param correlationIdFilter variable de entrada correlationIdFilter.
     */

    /**
     * Ejecuta la operación security config.
     * @param jwtTokenProvider variable de entrada jwtTokenProvider.
     * @param correlationIdFilter variable de entrada correlationIdFilter.
     * @return resultado de la operación security config.
     */

    public SecurityConfig(
            JwtTokenProvider jwtTokenProvider,
            CorrelationIdFilter correlationIdFilter,
            RequestObservabilityFilter requestObservabilityFilter
    ) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.correlationIdFilter = correlationIdFilter;
        this.requestObservabilityFilter = requestObservabilityFilter;
    }

    /**
     * Ejecuta la operación security filter chain.
     * @param http variable de entrada http.
     * @return resultado de la operación security filter chain.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        JwtAuthFilter jwtAuthFilter = new JwtAuthFilter(jwtTokenProvider);

        http
            .csrf(csrf -> csrf.disable())
            .cors(Customizer.withDefaults())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint((request, response, authException) ->
                    writeError(response, request, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized"))
                .accessDeniedHandler((request, response, accessDeniedException) ->
                    writeError(response, request, HttpServletResponse.SC_FORBIDDEN, "Forbidden"))
            )
            .authorizeHttpRequests(auth -> auth
                // ✅ CLAVE: permitir dispatchers de error/forward
                .dispatcherTypeMatchers(DispatcherType.ERROR, DispatcherType.FORWARD).permitAll()

                // ✅ Swagger / OpenAPI
                .requestMatchers(
                    "/v3/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html"
                ).permitAll()
                // ✅ Error endpoint (por las dudas)
                .requestMatchers("/error").permitAll()
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/predict/bulk-import").hasAuthority("ROLE_ADMIN")
                .requestMatchers(HttpMethod.POST, "/auth/register", "/auth/login", "/predict").permitAll()
                .requestMatchers(HttpMethod.GET, "/airports/**").permitAll()
                .requestMatchers("/predict/history").hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")
                .requestMatchers("/stats/model-accuracy").hasAuthority("ROLE_ADMIN")
                .requestMatchers("/stats/**").hasAuthority("ROLE_ADMIN")
                .anyRequest().authenticated()
            )
            .addFilterBefore(correlationIdFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterAfter(jwtAuthFilter, CorrelationIdFilter.class)
            .addFilterAfter(requestObservabilityFilter, JwtAuthFilter.class);
        return http.build();
    }
    /**
     * Ejecuta la operación password encoder.
     * @return resultado de la operación password encoder.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Ejecuta la operación cors configuration source.
     * @return resultado de la operación cors configuration source.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:3000",
                "http://192.168.92.33:3000"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
    /**
     * Ejecuta la operación write error.
     * @param response variable de entrada response.
     * @param request variable de entrada request.
     * @param status variable de entrada status.
     * @param message variable de entrada message.
     */

    private void writeError(
            HttpServletResponse response,
            HttpServletRequest request,
            int status,
            String message
    ) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(toJson(new ErrorResponse(
                OffsetDateTime.now(ZoneOffset.UTC),
                status,
                message,
                request.getRequestURI()
        )));
    }

    /**
     * Ejecuta la operación error response.
     * @param timestamp variable de entrada timestamp.
     * @param status variable de entrada status.
     * @param message variable de entrada message.
     * @param path variable de entrada path.
     * @return resultado de la operación error response.
     */

    /**
     * Record ErrorResponse.
     *
     * <p>Responsable de error response.</p>
     * @param timestamp variable de entrada timestamp.
     * @param status variable de entrada status.
     * @param message variable de entrada message.
     * @param path variable de entrada path.
     * @return resultado de la operación resultado.
     */

    private record ErrorResponse(
            OffsetDateTime timestamp,
            int status,
            String message,
            String path
    ) {
    }

    /**
     * Ejecuta la operación to json.
     * @param response variable de entrada response.
     * @return resultado de la operación to json.
     */

    private String toJson(ErrorResponse response) {
        if (response == null) {
            return "{}";
        }
        String timestamp = response.timestamp() == null
                ? ""
                : response.timestamp().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        return "{\"timestamp\":\"" + escapeJson(timestamp) + "\","
                + "\"status\":" + response.status() + ","
                + "\"message\":\"" + escapeJson(response.message()) + "\","
                + "\"path\":\"" + escapeJson(response.path()) + "\"}";
    }

    /**
     * Ejecuta la operación escape json.
     * @param value variable de entrada value.
     * @return resultado de la operación escape json.
     */

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
