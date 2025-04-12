package com.example.g_time.service;

import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import com.example.g_time.entity.User;
import com.example.g_time.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserService {
    @Autowired
    private UserMapper userMapper;

    public void registerUser(User user) {
        System.out.println(user);
        if (user.getPhone() == null || user.getPhone().isEmpty() &&
                (user.getEmail() == null || user.getEmail().isEmpty())) {
            throw new RuntimeException("手机号或邮箱不能为空");
        }
        user.setIntro("热爱生活，专注时间管理，让每一天都过得更有意义。");
        user.setAvatar("../../static/默认头像.png");
        user.setPhone(user.getPhone().equals("") ? null : user.getPhone());
        user.setEmail(user.getEmail().equals("") ? null : user.getEmail());
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.save(user);
    }

    public User isUser(String loginName, String loginPaw) {
        if (loginName.matches("^1[3-9]\\d{9}$")) {
            User user =  userMapper.selectUserByPhone(loginName);
            if(user.getPhone().equals(loginName) && user.getPassword().equals(loginPaw)) {
                return user;
            }
        }else if (loginName.matches("^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$")) {
            User user =  userMapper.selectUserByEmail(loginName);
            if (user.getEmail().equals(loginName) && user.getPassword().equals(loginPaw)) {
                return user;
            }
        }
        return null;
    }

}
