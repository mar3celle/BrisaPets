package com.brisapets.webapp.config;

import com.brisapets.webapp.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public SecurityConfig(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    // Regista o DaoAuthenticationProvider corretamente
    @Bean
    public DaoAuthenticationProvider authenticationProvider(PasswordEncoder encoder, UserDetailsService userDetailsService) {
        DaoAuthenticationProvider auth = new DaoAuthenticationProvider();
        auth.setUserDetailsService(userDetailsService);
        auth.setPasswordEncoder(encoder);
        return auth;
    }

    // Expõe o userService como UserDetailsService
    @Bean
    public UserDetailsService userDetailsService() {
        // Assume que seu UserService implementa UserDetailsService e tem findByEmail/loadUserByUsername
        return username -> (org.springframework.security.core.userdetails.UserDetails) userService.loadUserByUsername(username);
    }

    // Define as regras de acesso (URLs)
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .authorizeHttpRequests(requests -> requests
                        // ROTAS PÚBLICAS (Ajustado para ser limpo e claro)
                        .requestMatchers(
                                // 1. Recursos Estáticos (Sempre no topo)
                                "/css/**", "/js/**", "/images/**",

                                // 2. Rotas de Autenticação e Index
                                "/", "/entrar", "/login", "/register", "/autenticar", "/perform_login" // <-- Incluir o POST URL aqui!
                        ).permitAll()

                        // 3. O resto das rotas são privadas
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                                .loginPage("/autenticar")               // GET -> URL onde está a página HTML
                                .loginProcessingUrl("/perform_login") // POST -> URL para onde o formulário envia dados
                                .usernameParameter("username")
                                .passwordParameter("password")
                                .defaultSuccessUrl("/pets", true)
                        // IMPORTANTE: REMOVIDO o .permitAll() redundante daqui.
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                        .permitAll()
                );

        return http.build();
    }
}