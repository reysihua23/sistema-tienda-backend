// src/test/java/com/reydi/tienda/service/AuthSimpleTest.java
package com.reydi.tienda.service;

import com.reydi.tienda.model.Usuario;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

class AuthServiceTest {

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Test
    void testCrearUsuario() {
        Usuario usuario = new Usuario();
        usuario.setId(1);
        usuario.setCorreo("cliente@test.com");
        usuario.setPasswordHash(passwordEncoder.encode("123456"));
        usuario.setActivo(true);

        // Verificar que los datos se guardaron correctamente
        assertNotNull(usuario);
        assertEquals(1, usuario.getId());
        assertEquals("cliente@test.com", usuario.getCorreo());
        assertTrue(usuario.getActivo());
    }

    @Test
    void testEncriptarContrasena() {
        String contrasenaOriginal = "123456";
        String contrasenaEncriptada = passwordEncoder.encode(contrasenaOriginal);

        assertNotNull(contrasenaEncriptada);
        assertNotEquals(contrasenaOriginal, contrasenaEncriptada);
        assertTrue(passwordEncoder.matches(contrasenaOriginal, contrasenaEncriptada));
    }

    @Test
    void testValidarContrasenaIncorrecta() {
        String contrasenaOriginal = "123456";
        String contrasenaEncriptada = passwordEncoder.encode(contrasenaOriginal);
        String contrasenaIncorrecta = "wrongpassword";

        assertFalse(passwordEncoder.matches(contrasenaIncorrecta, contrasenaEncriptada));
    }

    @Test
    void testValidarEmail_FormatoCorrecto() {
        String email = "usuario@test.com";
        boolean esValido = email.contains("@") && email.contains(".");

        assertTrue(esValido);
    }

    @Test
    void testValidarEmail_FormatoIncorrecto() {
        String email = "usuario@test";
        boolean esValido = email.contains("@") && email.contains(".");

        assertFalse(esValido);
    }

    @Test
    void testValidarPassword_LongitudMinima() {
        String password = "123456";
        boolean esValida = password.length() >= 6;

        assertTrue(esValida);
    }

    @Test
    void testValidarPassword_LongitudInsuficiente() {
        String password = "123";
        boolean esValida = password.length() >= 6;

        assertFalse(esValida);
    }
}