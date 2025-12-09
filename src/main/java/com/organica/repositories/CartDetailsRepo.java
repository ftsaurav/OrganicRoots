package com.organica.repositories;

import com.organica.entities.Cart;
import com.organica.entities.CartDetalis;
import com.organica.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartDetailsRepo extends JpaRepository<CartDetalis,Integer> {
    public void deleteByProductAndCart(Product product, Cart cart);
    public CartDetalis findByProductAndCart(Product product, Cart cart);
}
