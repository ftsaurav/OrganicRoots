package com.organica.entities;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@ToString
public class CartDetalis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int cartDetalisId;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;   // <-- FIXED

    private int quantity;
    private int amount;

    @ManyToOne
    @JoinColumn(name = "cart_id")
    private Cart cart;
}

