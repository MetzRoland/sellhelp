package org.sellhelp.backend.configurations;

import org.sellhelp.backend.exceptions.CustomAccessDeniedHandler;
import org.sellhelp.backend.exceptions.CustomAuthenticationEntryPoint;
import org.sellhelp.backend.security.JWTFilter;
import org.sellhelp.backend.security.UserAuthDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
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
@EnableWebSecurity
public class SecurityConfig {

    private final JWTFilter jwtFilter;
    private final UserAuthDetailService userDetailService;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;

    @Autowired
    public SecurityConfig(JWTFilter jwtFilter, UserAuthDetailService userDetailService,
                          CustomAuthenticationEntryPoint customAuthenticationEntryPoint,
                          CustomAccessDeniedHandler customAccessDeniedHandler){
        this.jwtFilter = jwtFilter;
        this.userDetailService = userDetailService;
        this.customAuthenticationEntryPoint = customAuthenticationEntryPoint;
        this.customAccessDeniedHandler = customAccessDeniedHandler;
    }

    @Bean
    @Order(0)
    public SecurityFilterChain publicFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher(
                        "/api/public/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/api-docs/**",
                        "/webjars/**",
                        "/s3/**"
                        , "/user/files/download/*", "/user/files/public/*", "/user/files/public/*/pp", "/post/files/all/*", "/post/files/*/download"
                )
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                );

        return http.build();
    }

    @Bean
    @Order(1)
    public SecurityFilterChain jwtFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/user/**", "/superuser/**", "/post/**", "/api/**", "/auth/login",
                        "/auth/login/superuser",
                        "/auth/register",
                        "/auth/enable2fa",
                        "/auth/disable2fa",
                        "/auth/setup2fa",
                        "/auth/verify-totp",
                        "/auth/login/refresh",
                        "/auth/updateForgotPassword",
                        "/auth/forgotPasswordEmail")
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests((requests) -> requests
                        .requestMatchers("/auth/login", "/auth/login/superuser", "/auth/register", "/auth/verify-totp",
                                "/auth/login/refresh", "/auth/updateForgotPassword", "/auth/forgotPasswordEmail").permitAll()
                        .requestMatchers("/user/users/*").permitAll()
                        .requestMatchers("/post/posts/*", "/post/posts").permitAll()
                        .requestMatchers("/auth/enable2fa", "/auth/disable2fa", "/auth/setup2fa")
                        .authenticated()
                        .requestMatchers(
                                "/user/logout",
                                "/user/info",
                                "/user/update/details",
                                "/user/update/email",
                                "/user/update/password/send",
                                "/user/update/password",
                                "/user/files/pp/**",
                                "/user/files/public/*/pp"
                        ).hasAnyRole("ADMIN", "MODERATOR", "USER")
                        .requestMatchers("/user/**").hasRole("USER")
                        .requestMatchers("/post/**").hasRole("USER")
                        .requestMatchers("/superuser/**").hasAnyRole("ADMIN", "MODERATOR")
                        .anyRequest().authenticated()
                )
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint(customAuthenticationEntryPoint)
                        .accessDeniedHandler(customAccessDeniedHandler)
                )
                .userDetailsService(userDetailService)
        ;
        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain oauth2FilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/auth/login/google", "/auth/loginSuccess", "/oauth2/**", "/login/oauth2/**",
                        "/auth/google/register/**")
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                )
                .authorizeHttpRequests((requests) -> requests
                        .requestMatchers("/auth/login/google", "/auth/loginSuccess", "/oauth2/**", "/login/oauth2/**").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/auth/login/google")
                        .defaultSuccessUrl("/auth/loginSuccess", true)
                        .failureUrl("/loginFailure")
                )
        ;

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of(
                "http://localhost:5173",
                "http://www.sellhelp.org",
                "http://sellhelp.org",
                "https://www.sellhelp.org",
                "https://sellhelp.org",
                "https://accounts.google.com"
                ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setExposedHeaders(List.of("Authorization", "Set-Cookie"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManagerBean(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder
                .userDetailsService(userDetailService)
                .passwordEncoder(passwordEncoder());

        return authenticationManagerBuilder.build();
    }
}
