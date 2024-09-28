package com.littleblack.springbootmall.service;

import com.littleblack.springbootmall.dto.UserLoginRequest;
import com.littleblack.springbootmall.dto.UserRegisterRequest;
import com.littleblack.springbootmall.model.User;

public interface UserService {

    User getUserById(Integer userId);

    Integer register(UserRegisterRequest userRegisterRequest);

    User login(UserLoginRequest userLoginRequest);

}