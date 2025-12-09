package com.organica.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;


@NoArgsConstructor
@AllArgsConstructor
@Data
@ToString
public class CartDetailDto {

    private int cartDetalisId;
    private ProductDto product;
    private int quantity;
    private int amount;

//    private CartDto cart;
}
