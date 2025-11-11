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
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

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
        return userService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(requests -> requests
                        // 1. Recursos Estáticos (Sempre no topo)
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**").permitAll() // ✅ Adicionado /webjars/** para ser seguro

                        // 2. Rotas de Autenticação e Index
                        .requestMatchers("/", "/entrar", "/login", "/register", "/autenticar", "/perform_login").permitAll()

                        // Debug endpoint (temporary)
                        .requestMatchers("/debug/**").authenticated()

                        // ROTA DO PAINEL ADMIN: Requer a ROLE_ADMIN (mais idiomático)
                        .requestMatchers("/admin").hasRole("ADMIN")

                        // O resto das rotas são privadas (ex: /perfil, /pets, /appointments)
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/autenticar")
                        .loginProcessingUrl("/perform_login")
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/pets", true)
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "GET")) // Adiciona o RequestMatcher
                        .logoutSuccessUrl("/")
                        .invalidateHttpSession(true) // 🌟 NOVO: Invalida a sessão
                        .deleteCookies("JSESSIONID") // 🌟 NOVO: Deleta cookies de sessão
                        .permitAll()
                )
                // 🌟 NOVO: Tratamento de Exceção para Acesso Negado (403 Forbidden)
                .exceptionHandling(exceptions -> exceptions
                        .accessDeniedPage("/403") // Redireciona para uma página de erro 403
                )
                // Disable CSRF for API endpoints
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/admin/appointments/**")
                );


        return http.build();
    }
}
