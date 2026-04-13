package com.reydi.tienda.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "producto_imagenes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductoImagen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_prod_img")
    private Integer id;

    @Column(name = "url_imagen", nullable = false, length = 500)
    private String urlImagen;

    @Column(nullable = false)
    private Boolean principal = false;

    @Column(insertable = false, updatable = false)
    private LocalDateTime fecha;

    @ManyToOne
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;
}