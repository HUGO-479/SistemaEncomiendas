package org.example.config;

import org.example.usuario.service.UsuarioDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UsuarioDetailsService userDetailsService;
    private final LoginSuccessHandler loginSuccessHandler; // <-- Inyectamos el enrutador

    public SecurityConfig(UsuarioDetailsService userDetailsService, LoginSuccessHandler loginSuccessHandler) {
        this.userDetailsService = userDetailsService;
        this.loginSuccessHandler = loginSuccessHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // 1. RUTAS PÚBLICAS
                        .requestMatchers("/", "/index", "/login", "/consulta", "/error").permitAll()
                        .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()

                        // 2. RUTAS PROTEGIDAS
                        .requestMatchers("/cliente/**", "/mis-envios/**").hasRole("CLIENTE")
                        .requestMatchers("/empleado/**", "/recepcion/**").hasAnyRole("EMPLEADO", "ADMIN")
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .successHandler(loginSuccessHandler) // <-- Usamos nuestro enrutador
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout") // La ruta que usa el formulario
                        .logoutSuccessUrl("/login?logout") // A dónde redirige después de cerrar sesión
                        .invalidateHttpSession(true) // Borra la sesión
                        .deleteCookies("JSESSIONID") // Borra la cookie de sesión
                        .permitAll()
                );
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}