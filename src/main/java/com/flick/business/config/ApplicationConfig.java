package com.flick.business.config;

import com.flick.business.repository.security.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class ApplicationConfig implements WebMvcConfigurer {

    private final UserRepository userRepository;

    /**
     * Defines the user details provider (UserDetailsService).
     * This is the main component that Spring Security uses to fetch
     * user information (such as username and password) during authentication.
     */
    @Bean
    public UserDetailsService userDetailsService() {
        // We use a lambda expression to implement the 'loadUserByUsername' method.
        // It fetches the user from the database using our UserRepository.
        // If the user is not found, it throws a standard Spring Security exception.
        return username -> userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    }

    /**
     * Defines the authentication provider (AuthenticationProvider).
     * It combines the UserDetailsService (to fetch the user) and the
     * PasswordEncoder
     * (to verify the password).
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        // DaoAuthenticationProvider is Spring's default implementation for
        // database-based authentication.
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService()); // Sets how to fetch the user.
        authProvider.setPasswordEncoder(passwordEncoder()); // Sets how to verify the password.
        return authProvider;
    }

    /**
     * Exposes the AuthenticationManager as a Bean.
     * This component orchestrates the authentication process. We will use it
     * directly in our login endpoint.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Defines the password encoder (PasswordEncoder).
     * We use BCrypt, a strong industry-standard hashing algorithm for passwords.
     * Every password will be "scrambled" by this algorithm before being saved in
     * the database.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configures CORS to allow frontend on port 5173
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")  // Applies to all /api routes
                .allowedOrigins("http://localhost:5173")  // Frontend URL
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
