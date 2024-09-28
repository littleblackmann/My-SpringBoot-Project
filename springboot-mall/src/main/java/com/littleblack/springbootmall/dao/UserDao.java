package com.littleblack.springbootmall.dao;

import com.littleblack.springbootmall.dto.UserRegisterRequest;
import com.littleblack.springbootmall.model.User;

public interface UserDao {

    User getUserById(Integer userId);

    User getUserByEmail(String email);

    Integer createUser(UserRegisterRequest userRegisterRequest);

}