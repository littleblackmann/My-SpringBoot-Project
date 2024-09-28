package com.littleblack.springbootmall.controller;

import com.littleblack.springbootmall.dto.UserLoginRequest;
import com.littleblack.springbootmall.dto.UserRegisterRequest;
import com.littleblack.springbootmall.model.User;
import com.littleblack.springbootmall.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class UserController {

    @Autowired
    private UserService userService;



    @PostMapping("/users/register") // 註冊
    public ResponseEntity<User> register(@RequestBody @Valid UserRegisterRequest userRegisterRequest){
        Integer userId =  userService.register(userRegisterRequest);
        User user = userService.getUserById(userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @PostMapping("/users/login") // 登入
    public ResponseEntity<?> login(@RequestBody @Valid UserLoginRequest userLoginRequest){
        User user = userService.login(userLoginRequest);

        if (user != null) {
            // 登入成功，檢查是否為管理員
            boolean isAdmin = "king@gmail.com,".equals(user.getEmail());


            // 返回用戶信息和管理員狀態
            return ResponseEntity.status(HttpStatus.OK).body(new LoginResponse(user, isAdmin));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("登入失敗，請檢查您的郵箱和密碼。");
        }
    }

    // 用於返回登入響應的內部類
    private static class LoginResponse {
        private User user;
        private boolean isAdmin;

        public LoginResponse(User user, boolean isAdmin) {
            this.user = user;
            this.isAdmin = isAdmin;
        }

        public User getUser() {
            return user;
        }

        public boolean isAdmin() {
            return isAdmin;
        }
    }
}
