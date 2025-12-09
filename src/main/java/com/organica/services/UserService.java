package com.organica.services;

import com.organica.entities.User;
import com.organica.payload.SingIn;
import com.organica.payload.UserDto;

public interface UserService {


    UserDto CreateUser(UserDto userDto);

    SingIn SingIn(SingIn singIn);

    User getUserByEmail(String email);

}

