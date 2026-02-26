package io.github.mgluizbrito.topsoil_seeder_sample.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "Products")
@Getter
@Setter
@RequiredArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String sku;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, length = 1000)
    private String description;

    private BigDecimal price;

    @Column(name = "stockQT", nullable = false)
    private Integer stockQuantity;

    @Column(nullable = false)
    private Boolean active;
}
