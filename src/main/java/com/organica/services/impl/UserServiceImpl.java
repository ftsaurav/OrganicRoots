package com.organica.services.impl;

import com.organica.config.JwtService;
import com.organica.entities.Cart;
import com.organica.entities.Role;
import com.organica.entities.User;
import com.organica.payload.SingIn;
import com.organica.payload.UserDto;
import com.organica.repositories.UserRepo;
import com.organica.services.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // -------------------------------
    // SIGN UP
    // -------------------------------
    @Override
    public UserDto CreateUser(UserDto userDto) {

        // Convert DTO → Entity
        User user = this.modelMapper.map(userDto, User.class);

        // Set encoded password
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));

        // Set user role (BUYER or SELLER)
        if (userDto.getRole() == null) {
            user.setRole(Role.BUYER); // default role if none selected
        } else {
            user.setRole(userDto.getRole());
        }

        // Create empty cart for the user
        Cart cart = new Cart();
        cart.setUser(user);
        user.setCart(cart);

        // Save to DB
        User saved = this.userRepo.save(user);

        // Convert back to DTO
        UserDto response = this.modelMapper.map(saved, UserDto.class);
        response.setPassword(null); // never return password

        return response;
    }

    // -------------------------------
    // SIGN IN
    // -------------------------------
    @Override
    public SingIn SingIn(SingIn singIn) {

        // Validate credentials
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        singIn.getEmail(),
                        singIn.getPassword()
                )
        );

        // Fetch user from DB
        User user = this.userRepo.findByEmail(singIn.getEmail());

        // Generate JWT token
        String jwtToken = jwtService.generateToken(user);

        // Set details in response
        singIn.setJwt(jwtToken);
        singIn.setRole(user.getRole().name());
        singIn.setUsername(user.getUsername());

        return singIn;
    }

    @Override
    public User getUserByEmail(String email) {
        return userRepo.findByEmail(email);
    }

}

