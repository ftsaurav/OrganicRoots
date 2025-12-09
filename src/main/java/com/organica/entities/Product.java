package com.organica.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Entity
@NoArgsConstructor
@Data
@ToString
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int productId;

    @Column(nullable = false)
    private String productName;

    private String description;

    private Float price;

    private Float weight;

    @Column(length = 65555)
    private byte[] img;

    @OneToMany(mappedBy = "product")
    private List<CartDetalis> cartDetails;
}
