package com.organica.services.impl;

import com.organica.entities.Cart;
import com.organica.entities.CartDetalis;
import com.organica.entities.Product;
import com.organica.entities.User;
import com.organica.payload.*;
import com.organica.repositories.CartDetailsRepo;
import com.organica.repositories.CartRepo;
import com.organica.repositories.ProductRepo;
import com.organica.repositories.UserRepo;
import com.organica.services.CartService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private ProductRepo productRepo;

    @Autowired
    private CartRepo cartRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private CartDetailsRepo cartDetailsRepo;

    @Autowired
    private ModelMapper modelMapper;



    @Override
    public CartDto CreateCart(CartHelp cartHelp) {
        return null;
    }

    @Override
    public CartDto addProductToCart(CartHelp cartHelp) {

        int productId = cartHelp.getProductId();
        System.out.println("DEBUG -> productId received = " + productId);
        if (productId <= 0) {
            throw new RuntimeException("Invalid Product ID received: " + productId);
        }

        int quantity = cartHelp.getQuantity();
        String userEmail = cartHelp.getUserEmail();

        // -------------------------
        // Validate User
        // -------------------------
        User user = userRepo.findByEmail(userEmail);
        if (user == null) {
            throw new RuntimeException("User not found with email: " + userEmail);
        }

        // -------------------------
        // Validate Product
        // -------------------------
        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with ID: " + productId));

        // -------------------------
        // Get or Create Cart
        // -------------------------
        Cart cart = user.getCart();

        if (cart == null) {
            cart = new Cart();
            cart.setUser(user);

            List<CartDetalis> list = new ArrayList<>();

            CartDetalis cd = new CartDetalis();
            cd.setProduct(product);
            cd.setQuantity(quantity);
            cd.setAmount((int) (product.getPrice() * quantity));
            cd.setCart(cart);

            list.add(cd);

            cart.setCartDetalis(list);
            cart.setTotalAmount(cd.getAmount());

            Cart savedCart = cartRepo.save(cart);

            return convertToDto(savedCart);
        }

        // -------------------------
        // UPDATE EXISTING CART
        // -------------------------
        List<CartDetalis> list = cart.getCartDetalis();
        AtomicInteger totalAmount = new AtomicInteger(0);
        AtomicBoolean updated = new AtomicBoolean(false);

        for (CartDetalis cd : list) {

            if (cd.getProduct() != null && cd.getProduct().getProductId() == productId) {
                cd.setQuantity(quantity);
                cd.setAmount((int) (quantity * product.getPrice()));
                updated.set(true);
            }

            totalAmount.addAndGet(cd.getAmount());
        }

        if (!updated.get()) {
            CartDetalis newCd = new CartDetalis();
            newCd.setProduct(product);
            newCd.setQuantity(quantity);
            newCd.setAmount((int) (quantity * product.getPrice()));
            newCd.setCart(cart);

            list.add(newCd);
            totalAmount.addAndGet(newCd.getAmount());
        }

        cart.setCartDetalis(list);
        cart.setTotalAmount(totalAmount.get());

        Cart saved = cartRepo.save(cart);

        return convertToDto(saved);
    }


    // -----------------------------------------------------------
// Helper method to convert Cart → CartDto safely
// -----------------------------------------------------------
    private CartDto convertToDto(Cart cart) {
        CartDto dto = modelMapper.map(cart, CartDto.class);

        for (CartDetailDto cd : dto.getCartDetalis()) {

            if (cd.getProduct() != null && cd.getProduct().getImg() != null) {
                cd.getProduct().setImg(
                        decompressBytes(cd.getProduct().getImg())
                );
            }
        }



        return dto;
    }


    @Override
    public CartDto GetCart(String userEmail) {
        User user = this.userRepo.findByEmail(userEmail);
        Cart byUser = this.cartRepo.findByUser(user);



    // img decompressBytes
        CartDto map = this.modelMapper.map(byUser, CartDto.class);
        List<CartDetailDto> cartDetalis1 = map.getCartDetalis();


        for (CartDetailDto i:cartDetalis1 ) {
            ProductDto p=i.getProduct();
            p.setImg(decompressBytes(p.getImg()));
        }
        map.setCartDetalis(cartDetalis1);
        return map;
    }

    @Override
    public void RemoveById(Integer productId, String userEmail) {
        User user = this.userRepo.findByEmail(userEmail);

        Product product = this.productRepo.findById(productId).orElseThrow();
        Cart cart =this.cartRepo.findByUser(user);

        CartDetalis byProductAndCart = this.cartDetailsRepo.findByProductAndCart(product, cart);
        int amount = byProductAndCart.getAmount();
        cart.setTotalAmount(cart.getTotalAmount()-amount);
        this.cartRepo.save(cart);

        this.cartDetailsRepo.delete(byProductAndCart);


    }

    @Override
    public int getCartProductCount(String userEmail) {

        // 1. Find user by email
        User user = userRepo.findByEmail(userEmail);
        if (user == null) {
            throw new RuntimeException("User not found with email: " + userEmail);
        }


        // 2. Find cart by user
        Cart cart = cartRepo.findByUser(user);

        if (cart == null || cart.getCartDetalis() == null) {
            return 0;
        }

        // 3. Return number of items in cart
        return cart.getCartDetalis().size();
    }











    public Product changeImg(Product product){

        product.setImg(decompressBytes(product.getImg()));

        System.out.println("hello");
        return product;
    }

    public int totalP(int t1, int total){
        return total+t1;
    }



    public static byte[] decompressBytes(byte[] data) {
        if (data == null || data.length == 0) {
            return null;  // IMPORTANT FIX
        }

        try {
            Inflater inflater = new Inflater();
            inflater.setInput(data);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
            byte[] buffer = new byte[1024];

            while (!inflater.finished()) {
                int count = inflater.inflate(buffer);
                outputStream.write(buffer, 0, count);
            }

            return outputStream.toByteArray();
        } catch (Exception e) {
            return null;  // fail-safe
        }
    }

}
