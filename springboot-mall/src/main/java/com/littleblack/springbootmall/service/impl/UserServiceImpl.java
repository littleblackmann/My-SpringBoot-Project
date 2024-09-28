package com.littleblack.springbootmall.service.impl;

import com.littleblack.springbootmall.dao.UserDao;
import com.littleblack.springbootmall.dto.UserLoginRequest;
import com.littleblack.springbootmall.dto.UserRegisterRequest;
import com.littleblack.springbootmall.model.User;
import com.littleblack.springbootmall.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;
import org.springframework.web.server.ResponseStatusException;

@Component
public class UserServiceImpl implements UserService {

    private final static Logger log = LoggerFactory.getLogger(UserServiceImpl.class); // 用來記錄日誌


    @Autowired
    private UserDao userDao; // 用來操作資料庫的 DAO

    @Override
    public User getUserById(Integer userId) { // 透過 userId 取得使用者資訊
        return userDao.getUserById(userId);
    }

    @Override
    public Integer register(UserRegisterRequest userRegisterRequest) { // 註冊使用者
        // 檢查註冊的Email是否已經存在
        User user = userDao.getUserByEmail(userRegisterRequest.getEmail()); // 透過 Email 取得使用者資訊

        if (user != null) { // 如果使用者已經存在
            log.warn("該 Email {} 已經被註冊" , userRegisterRequest.getEmail()); // 記錄警告日誌
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST); // 拋出錯誤
        }

        // 使用 MD5 生成密碼雜湊值
        String hashedPassword = DigestUtils.md5DigestAsHex(userRegisterRequest.getPassword().getBytes());
        userRegisterRequest.setPassword(hashedPassword);

        // 創建使用者
        return userDao.createUser(userRegisterRequest);
    }

    @Override
    public User login(UserLoginRequest userLoginRequest) {
        // 檢查登入的Email是否已經存在
        User user = userDao.getUserByEmail(userLoginRequest.getEmail());

        // 檢查User是否存在
        if (user == null) {
            log.warn("該 Email {} 尚未註冊", userLoginRequest.getEmail());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        // 使用 MD5 生成密碼雜湊值
        String hashedPassword = DigestUtils.md5DigestAsHex(userLoginRequest.getPassword().getBytes());

        // 檢查密碼是否正確
        if (user.getPassword().equals(hashedPassword)) {
            return user;
        } else {
            log.warn("Email {} 的密碼不正確", userLoginRequest.getEmail());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }



}