package com.organica.payload;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SingIn {

    private String email;
    private String password;

    private String jwt;       // token returned after login
    private String role;      // BUYER / SELLER
    private String username;  // user's display name
}
