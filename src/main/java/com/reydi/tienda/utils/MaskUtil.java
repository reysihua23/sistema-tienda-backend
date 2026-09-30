package com.reydi.tienda.utils;

public class MaskUtil {

    /**
     * Enmascara un documento mostrando solo los últimos N caracteres.
     * Ej: maskDocumento("98765432", 2) → "******32"
     */
    public static String maskDocumento(String doc, int visibleChars) {
        if (doc == null || doc.isBlank()) return doc;
        if (doc.length() <= visibleChars) return doc;
        int masked = doc.length() - visibleChars;
        return "*".repeat(masked) + doc.substring(masked);
    }

    /**
     * Enmascara un teléfono.
     * Ej: maskTelefono("904003198", 3) → "******198"
     */
    public static String maskTelefono(String tel, int visibleChars) {
        if (tel == null || tel.isBlank()) return tel;
        if (tel.length() <= visibleChars) return tel;
        int masked = tel.length() - visibleChars;
        return "*".repeat(masked) + tel.substring(masked);
    }

    /**
     * Enmascara un correo.
     * Ej: maskEmail("reysihua23@gmail.com") → "r***3@gmail.com"
     */
    public static String maskEmail(String email) {
        if (email == null || !email.contains("@")) return email;
        int at = email.indexOf("@");
        String user = email.substring(0, at);
        String domain = email.substring(at);
        if (user.length() <= 2) return user.charAt(0) + "***" + domain;
        return user.charAt(0) + "***" + user.charAt(user.length() - 1) + domain;
    }
}