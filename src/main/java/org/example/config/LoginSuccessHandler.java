package org.example.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;

@Component
public class LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        Collection<SimpleGrantedAuthority> authorities = (Collection<SimpleGrantedAuthority>) authentication.getAuthorities();
        String rol = authorities.iterator().next().getAuthority();

        String targetUrl = "/";
        if (rol.equals("ROLE_ADMIN")) {
            targetUrl = "/admin/dashboard";
        } else if (rol.equals("ROLE_EMPLEADO")) {
            targetUrl = "/empleado/dashboard";
        } else if (rol.equals("ROLE_CLIENTE")) {
            targetUrl = "/cliente/dashboard";
        }

        // Redirige a la URL según el rol
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}