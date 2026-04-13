package com.reydi.tienda.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reclamo_evidencias")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReclamoEvidencias {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_recl_evid")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "reclamo_id", nullable = false)
    private Reclamo reclamo;

    @Column(name = "url_imagen", nullable = false, length = 500)
    private String urlImagen;

    @Column(insertable = false, updatable = false)
    private LocalDateTime fecha;


}