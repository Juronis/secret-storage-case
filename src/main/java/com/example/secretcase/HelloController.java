package com.example.secretcase;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    private static final String WWW_AUTHENTICATE = "Basic realm=\"secret-case\"";

    private final String adminUsername;
    private final String adminPassword;

    public HelloController(
            @Value("${app.admin-username}") String adminUsername,
            @Value("${app.admin-password}") String adminPassword) {
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
    }

    @GetMapping("/")
    public ResponseEntity<String> hello(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Basic ")) {
            return unauthorized("Unauthorized");
        }

        String decoded;
        try {
            decoded = new String(
                    Base64.getDecoder().decode(authHeader.substring(6).trim()),
                    StandardCharsets.UTF_8);
        } catch (IllegalArgumentException ex) {
            return unauthorized("Invalid credentials");
        }

        String[] parts = decoded.split(":", 2);
        if (parts.length == 2 && adminUsername.equals(parts[0]) && adminPassword.equals(parts[1])) {
            return ResponseEntity.ok("Hello, welcome to the secret case!");
        }

        return unauthorized("Invalid credentials");
    }

    private static ResponseEntity<String> unauthorized(String body) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .header("WWW-Authenticate", WWW_AUTHENTICATE)
                .body(body);
    }
}
