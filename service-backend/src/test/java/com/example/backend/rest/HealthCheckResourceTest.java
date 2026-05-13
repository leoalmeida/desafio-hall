package com.example.backend.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class HealthCheckResourceTest {

    @Test
    void greetingDeveRetornarHelloWorld() {
        HealthCheckResource resource = new HealthCheckResource();
        assertEquals("Hello, World", resource.greeting());
    }

    @Test
    void maxAgeDeveSerTresMillSeiscentos() {
        assertEquals(3600L, HealthCheckResource.MAX_AGE);
    }
}
