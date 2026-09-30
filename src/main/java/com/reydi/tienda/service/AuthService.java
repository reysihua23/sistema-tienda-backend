package com.reydi.tienda.service;

import com.reydi.tienda.dto.LoginRequest;
import com.reydi.tienda.dto.LoginResponse;

public interface AuthService {

    /**
     * Autentica un usuario con correo y contraseña.
     *
     * @param request contiene correo y password
     * @return LoginResponse con token JWT + datos del usuario
     * @throws RuntimeException si las credenciales son inválidas
     */
    LoginResponse login(LoginRequest request);
}