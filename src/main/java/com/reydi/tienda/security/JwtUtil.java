package com.reydi.tienda.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    private static final String SECRET = "miClaveSecretaParaJWT1234567890!@#$%^&*()_+";
    private static final long EXPIRATION_TIME = 86400000; // 24 horas

    private final Key key = Keys.hmacShaKeyFor(SECRET.getBytes());

    public String generarToken(String correo, String rol, Integer usuarioId) {
        return Jwts.builder()
                .setSubject(correo)
                .claim("rol", rol)
                .claim("usuarioId", usuarioId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String extraerCorreo(String token) {
        return extraerClaims(token).getSubject();
    }

    public String extraerRol(String token) {
        return extraerClaims(token).get("rol", String.class);
    }

    public Integer extraerUsuarioId(String token) {
        return extraerClaims(token).get("usuarioId", Integer.class);
    }

    public boolean validarToken(String token) {
        try {
            extraerClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Claims extraerClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}