package com.littleblack.springbootmall.controller;

import com.littleblack.springbootmall.dto.UserLoginRequest;
import com.littleblack.springbootmall.dto.UserRegisterRequest;
import com.littleblack.springbootmall.model.User;
import com.littleblack.springbootmall.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
public class UserController {

    private final static Logger log = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @PostMapping("/users/register")
    public ResponseEntity<User> register(@RequestBody @Valid UserRegisterRequest userRegisterRequest){
        Integer userId = userService.register(userRegisterRequest);
        User user = userService.getUserById(userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @PostMapping("/users/login")
    public ResponseEntity<?> login(@RequestBody @Valid UserLoginRequest userLoginRequest,
                                   HttpSession session) {
        try {
            User user = userService.login(userLoginRequest);

            if (user != null) {
                // 將用戶資訊存入 session
                session.setAttribute("userId", user.getUserId());
                session.setAttribute("userEmail", user.getEmail());

                // 檢查是否為管理員
                boolean isAdmin = "king@gmail.com".equals(user.getEmail());

                // 生成簡單的 session token
                String sessionToken = java.util.UUID.randomUUID().toString();
                session.setAttribute("sessionToken", sessionToken);

                // 構建響應數據
                LoginResponse response = new LoginResponse(user, isAdmin, sessionToken);

                log.info("User {} successfully logged in", user.getEmail());
                return ResponseEntity.ok(response);
            } else {
                log.warn("Login failed: invalid credentials");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("登入失敗，請檢查您的郵箱和密碼。");
            }
        } catch (Exception e) {
            log.error("Login error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("登入過程中發生錯誤，請稍後再試。");
        }
    }

    @PostMapping("/users/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        try {
            // 獲取當前用戶信息用於日誌記錄
            Integer userId = (Integer) session.getAttribute("userId");
            String userEmail = (String) session.getAttribute("userEmail");

            if (userId != null) {
                log.info("User {} logging out", userEmail);
                // 清除 session
                session.invalidate();
                return ResponseEntity.ok("登出成功");
            } else {
                log.warn("Logout attempted with no active session");
                return ResponseEntity.ok("用戶已登出");
            }
        } catch (Exception e) {
            log.error("Logout error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("登出過程中發生錯誤");
        }
    }

    private static class LoginResponse {
        private User user;
        private boolean isAdmin;
        private String token;

        public LoginResponse(User user, boolean isAdmin, String token) {
            this.user = user;
            this.isAdmin = isAdmin;
            this.token = token;
        }

        public User getUser() {
            return user;
        }

        public boolean isAdmin() {
            return isAdmin;
        }

        public String getToken() {
            return token;
        }
    }
}