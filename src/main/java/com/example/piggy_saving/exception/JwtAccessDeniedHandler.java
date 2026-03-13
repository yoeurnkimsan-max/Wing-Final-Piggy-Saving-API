package com.example.piggy_saving.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;

public class JwtAccessDeniedHandler implements AccessDeniedHandler {
    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");
        response.getWriter().write(
                "{\"timestamp\":\"" + java.time.LocalDateTime.now() + "\"," +
                        "\"status\":403," +
                        "\"error\":\"Forbidden\"," +
                        "\"message\":\"Access Denied\"," +
                        "\"path\":\"" + request.getRequestURI() + "\"}"
        );
    }
}
