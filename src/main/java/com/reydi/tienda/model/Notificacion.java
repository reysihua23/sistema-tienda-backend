package com.reydi.tienda.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notificaciones")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_notificacion")
    private Integer id;

    @Column(length = 50)
    private String tipo;

    @Column(length = 255)
    private String mensaje;

    @Column(nullable = false)
    private Boolean leido = false;

    @Column(insertable = false, updatable = false)
    private LocalDateTime fecha;
}