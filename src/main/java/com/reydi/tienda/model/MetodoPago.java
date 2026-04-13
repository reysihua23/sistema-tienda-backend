package com.reydi.tienda.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum MetodoPago {
    TARJETA,
    YAPE,
    PLIN,
    TRANSFERENCIA,
    EFECTIVO,
    PAYPAL;
    @JsonCreator
    public static MetodoPago fromString(String value) {
        if (value == null) return null;
        return MetodoPago.valueOf(value.toUpperCase());
    }

    @JsonValue
    public String toValue() {
        return this.name();
    }
}