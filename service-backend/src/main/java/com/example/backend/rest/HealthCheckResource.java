package com.example.backend.rest;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(
        origins = {"http://localhost:80", "http://localhost:4200"},
        maxAge = HealthCheckResource.MAX_AGE)
@RequestMapping("/api/healthcheck")
public class HealthCheckResource {
    public static final long MAX_AGE = 3600L;

    @GetMapping("/")
    public String greeting() {
        return "Hello, World";
    }
}
