package org.akhq.controllers;

import io.micronaut.http.HttpRequest;
import io.micronaut.rxjava2.http.client.RxHttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.akhq.AbstractTest;
import org.junit.jupiter.api.Test;

import jakarta.inject.Inject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HeaderAuthControllerTest extends AbstractTest {
    @Inject
    @Client("/")
    protected RxHttpClient client;

    @Test
    void user() {
        AkhqController.AuthUser result = client.toBlocking().retrieve(
            HttpRequest
                .GET("/api/me")
                .header("x-akhq-user", "header-user"),
            AkhqController.AuthUser.class
        );

        assertEquals("header-user", result.getUsername());
        assertEquals(2, result.getRoles().size());
    }

    @Test
    void admin() {
        AkhqController.AuthUser result = client.toBlocking().retrieve(
            HttpRequest
                .GET("/api/me")
                .header("x-akhq-user", "header-admin"),
            AkhqController.AuthUser.class
        );

        assertEquals("header-admin", result.getUsername());
        assertEquals(2, result.getRoles().size());
    }

    @Test
    void externalUserAndGroup() {
        AkhqController.AuthUser result = client.toBlocking().retrieve(
            HttpRequest
                .GET("/api/me")
                .header("x-akhq-user", "header-user-operator")
                .header("x-akhq-group", "external-operator,external-limited"),
            AkhqController.AuthUser.class
        );

        assertEquals("header-user-operator", result.getUsername());
        assertEquals(5, result.getRoles().size());
    }

    @Test
    void userWithAdditionalExternalGroup() {
        AkhqController.AuthUser result = client.toBlocking().retrieve(
            HttpRequest
                .GET("/api/me")
                .header("x-akhq-user", "header-user")
                .header("x-akhq-group", "external-limited"),
            AkhqController.AuthUser.class
        );

        assertEquals("header-user", result.getUsername());
        // operator from 'users' and externally provided 'limited'
        assertEquals(5, result.getRoles().size());
    }

    @Test
    void userWithoutAnyGroup() {
        AkhqController.AuthUser result = client.toBlocking().retrieve(
            HttpRequest
                .GET("/api/me")
                .header("x-akhq-user", "header-invalid"),
            AkhqController.AuthUser.class
        );

        assertEquals("header-invalid", result.getUsername());
        assertTrue(result.getRoles().isEmpty());
    }

    @MicronautTest(environments = "header-ip-disallow")
    static class UntrustedIp extends AbstractTest {
        @Inject
        @Client("/")
        protected RxHttpClient client;

        @Test
        void invalidIp() {
            AkhqController.AuthUser result = client.toBlocking().retrieve(
                HttpRequest
                    .GET("/api/me")
                    .header("x-akhq-user", "header-user")
                    .header("x-akhq-group", "limited,extra"),
                AkhqController.AuthUser.class
            );

            assertNull(result.getUsername());
            assertNull(result.getRoles());
        }
    }
}
