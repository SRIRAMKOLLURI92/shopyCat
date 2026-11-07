package com.shopycat.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // Browsing is open to visitors; each post's audience is still checked by PrivacyPolicy.
                .requestMatchers("/shops/new", "/shops").hasRole("SHOP_OWNER")
                .requestMatchers("/offers/new", "/listings/new").authenticated()
                .requestMatchers("/", "/register", "/login", "/css/**", "/js/**", "/error").permitAll()
                .requestMatchers(HttpMethod.GET, "/shops/{id}", "/offers/{id}", "/listings/{id}").permitAll()
                .anyRequest().authenticated())
            .formLogin(form -> form
                .loginPage("/login")
                .usernameParameter("email")
                .defaultSuccessUrl("/dashboard", true)
                .permitAll())
            .logout(logout -> logout.logoutSuccessUrl("/?loggedOut").permitAll());
        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
